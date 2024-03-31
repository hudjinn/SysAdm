from zoneinfo import ZoneInfo
from datetime import datetime
import os
import json
from functools import wraps

from flask import Flask, render_template, request, redirect, url_for, session, flash
import requests


app = Flask(__name__, static_url_path='/static')
app.secret_key = os.urandom(24)

# Caminho absoluto do arquivo em execução
caminho_absoluto = os.path.abspath(__file__)

# Diretório do arquivo em execução
root = os.path.dirname(caminho_absoluto)

# Diretório dos textos de internacionalização
text_path =  root + '/locate'

#API JAVA
app.api = 'http://sysadm-api:8080/usuario'

def check_api_status(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            response = requests.get(app.api)  # Supondo que app.api é sua URL base da API
            if response.status_code == 200:
                return f(*args, **kwargs)
            else:
                flash(session['text']['conn_error'], 'error')
                return redirect(url_for('login_screen'))
        except requests.exceptions.RequestException:
            flash(session['text']['conn_error'], 'error')
            return redirect(url_for('login_screen'))
    return decorated_function

@app.before_request
def before_request():
    # Define um idioma padrão se nenhum tiver sido selecionado ainda.
    if 'language' not in session:
        session['language'] = 'pt_BR'
        session['text'] = read_translation(session['language'])
    else:
        session['text'] = read_translation(session['language'])


@app.route('/set_language', methods=['POST'])
def set_language():
    language = request.form.get('language')
    if language:
        session['language'] = language
        # Carregar as traduções para o idioma escolhido e salvar na sessão.
        session['text'] = read_translation(language)
        
    # Redirecionar para a página de onde veio, ou para a página inicial se o referenciador não estiver disponível.
    return redirect(request.referrer or url_for('login_screen'))

# Ler arquivo de internacionalização
def read_translation(language):

    file_path = f'{text_path}/{language}.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        translations = json.load(file)
    return translations

# Rota para a página de login
@app.route('/', methods=['GET', 'POST'])
def login_screen():
    max_tries = 3
    wait_time = 60  # Tempo de espera em segundos

    # Certifique-se de que 'last_try_time' esteja sempre offset-aware
    if 'tries' not in session:
        session['tries'] = 0
        # Armazena o tempo como offset-aware usando UTC
        session['last_try_time'] = datetime.now(ZoneInfo("UTC")).isoformat()

    # Converter a string ISO de volta para datetime e torná-lo offset-aware
    last_try_time = datetime.fromisoformat(session['last_try_time']).replace(tzinfo=ZoneInfo("UTC"))
    now_aware = datetime.now(ZoneInfo("UTC"))

    # Calcular o tempo desde a última tentativa
    time_since_last_try = now_aware - last_try_time
    if time_since_last_try.seconds < wait_time and session['tries'] >= max_tries:
        wait_seconds = wait_time - time_since_last_try.seconds
        error_message = session['text']['wait_before_retry'].format(wait_seconds)
        return render_template('login_screen.html', text=session['text'], error=error_message)

    if request.method == 'POST':
        # Aqui você captura os dados do formulário e envia para a API Java
        email = request.form['email_login']
        senha = request.form['senha_login']
        
        try:
            response = requests.post(app.api + '/login', json={'email': email, 'senha': senha})
            if not response.ok:
                session['tries'] += 1
                session['last_try_time'] = datetime.now(ZoneInfo("UTC")).isoformat()
                if session['tries'] >= max_tries:
                    error_message = session['text']['max_attempts'].format(wait_time)
                else:
                    error_message = session['text']['login_failed']
                flash(error_message, 'error')  
                return redirect(url_for('login_screen'))

            elif response.ok:
                # Reseta as tentativas após login bem-sucedido
                session.pop('tries', None)
                session.pop('last_try_time', None)
                session['user'] = email
                return redirect(url_for('admin_screen'))
            else:
                error_message = session['text']['unknown_error']
                return render_template('login_screen.html', text=session['text'], error=error_message)
        except requests.exceptions.ConnectionError:
            # Captura o caso em que a API está offline ou o hostname não pode ser resolvido
            flash(session['text']['conn_error'], 'error')
        
        # Redireciona para a página de login novamente ou para onde você achar adequado
        return redirect(url_for('login_screen'))

    # Se o método for GET, apenas exiba a tela de login.
    return render_template('login_screen.html', text=session['text'])


# Rota para a página de cadastro
@app.route('/create_account', methods=['GET', 'POST'])
def create_account_screen():
    if request.method == 'POST':
        # Captura os dados do formulário

        data_nasc = request.form['data_nasc_cad']
        data_nasc_formatted = datetime.strptime(data_nasc, "%Y-%m-%d").strftime("%Y-%m-%d")

        user_data = {
            "nome": request.form['nome_cad'],
            "email": request.form['email_cad'],
            "dataNasc": data_nasc_formatted,
            "cpf": request.form['cpf_cad'],
            "senha": request.form['senha_cad']
        }

        # Envia os dados do usuário para a API
        response = requests.post( app.api + 'cadastrar', json=user_data)

        if response.status_code == 201:
            # Usuário criado com sucesso, redirecionar para a tela de login ou outra página
            flash(session['text']['create_acc_success'], 'success')
            return redirect(url_for('login_screen'))
        else:
            flash(f"{response.text} {session['text']['create_acc_fail']} ", 'error')


    # Se o método for GET ou se o cadastro falhar, mostra a tela de cadastro novamente
    return render_template('create_acc_screen.html', text=session['text'])

# Rota para a página de recuperação de senha
@app.route('/recover_password', methods=['GET', 'POST'])
@check_api_status
def recover_password():
    if request.method == 'POST':
        
        email = request.form['email_recovery']
        cpf = request.form['cpf_recovery']
        data_nasc = request.form['data_nasc_recovery']

        # TODO checar na API validade
        response = requests.post(app.api , json={'email': email,
                                                 'cpf': cpf,
                                                 'data_nasc': data_nasc})
        if response.ok:
            return redirect(url_for('change_password.html'), text=session['text'])
        else:
            error_message = session['text']['form_dont_match']
            return render_template('recover_password.html', text=session['text'], error=error_message)

    # Se o método for GET ou se o validação falhar, mostra a tela de recuperaçao de senha novamente   
    return render_template('recover_password.html', text=session['text'])

# Rota para a página de alteração de senha
@app.route('/change_password/<email>', methods=['GET', 'POST', 'PUT'])
@check_api_status

def change_password(email):

    if request.method == 'POST':
        # Processar o formulário de alteração de senha
        nova_senha = request.form['nova_senha']
        confirma_senha = request.form['confirma_senha']
        if nova_senha == confirma_senha:
            # TODO Metodo PUT para alterar usuario
            return redirect(url_for('password_changed', text=session['text'], email=email))
        else:
            # Senhas não coincidem, exibir mensagem de erro
            error_message = session['text']['passwords_dont_match']
            return render_template('change_password.html', text=session['text'], error_message=error_message, email=email)
    else:
        return render_template('change_password.html', text=session['text'], email=email)

# Rota para a página de sucesso na alteração de senha
@app.route('/password_changed')

def password_changed(email):
    return render_template('password_changed.html', text=session['text'], email=email)

# Rota para a página de administração do site

@app.route('/admin_screen', methods=['GET', 'POST', 'PUT', 'DELETE'])
@check_api_status

def admin_screen():
    # TODO Enviar permissões possíveis
    return render_template('admin_screen.html', text=session['text'])
    
if __name__ == '__main__':
    app.run(debug=True)
    
    