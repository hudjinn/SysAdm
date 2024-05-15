import time
from zoneinfo import ZoneInfo
from datetime import datetime
import os
import json
from functools import wraps

from flask import Flask, jsonify, render_template, request, redirect, url_for, session, flash, g
import requests


app = Flask(__name__, static_url_path='/static')
app.secret_key = os.urandom(24)

# Caminho absoluto do arquivo em execução
caminho_absoluto = os.path.abspath(__file__)

# Diretório do arquivo em execução
root = os.path.dirname(caminho_absoluto)

# Diretório dos textos de internacionalização
text_path =  root + '/locate'

# API JAVA
app.api = 'http://sysadm-api:8080/'


def check_api_status(f):
    @wraps(f)
    def decorated_function(*args, **kwargs):
        try:
            response = requests.get(app.api + 'usuarios')
            if response.status_code == 200:
                return f(*args, **kwargs)
            else:
                flash(session['flash_text']['conn_error'], 'error')
                return redirect(url_for('login_screen'))
        except requests.exceptions.RequestException:
            flash(session['flash_text']['conn_error'], 'error')
            return redirect(url_for('login_screen'))
    return decorated_function

def wait_for_api(api_url, timeout=300, interval=10):
    """
    Espera até que a API esteja disponível ou o timeout seja alcançado.

    :param api_url: URL da API a ser verificada.
    :param timeout: Tempo máximo de espera em segundos.
    :param interval: Intervalo entre as verificações em segundos.
    """
    start_time = time.time()
    while time.time() - start_time < timeout:
        try:
            response = requests.get(api_url)
            if response.status_code == 200:
                print("API está online.")
                return True
        except requests.exceptions.RequestException as e:
            print(f"API não disponível, tentando novamente em {interval} segundos... Erro: {e}")
        time.sleep(interval)
    print("Timeout alcançado, a API não está disponível.")
    return False

@app.before_request
def before_request():
    # Define um idioma padrão se nenhum tiver sido selecionado ainda.
    if 'language' not in session:
        session['language'] = 'pt_BR'
        session['text'] = read_translation(session['language'])
        session['flash_text'] = read_flash_translations(session['language'])

    else:
        session['text'] = read_translation(session['language'])
        session['flash_text'] = read_flash_translations(session['language'])


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

# Ler arquivo de internacionalização para flash messages
def read_flash_translations(language):
    file_path = f'{text_path}/flash_{language}.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        translations = json.load(file)
    return translations

def login_required(f):
    def decorated_function(*args, **kwargs):
        if 'logged_in' in session:
            return f(*args, **kwargs)
        else:
            flash(session['flash_text']['login_required'], 'error')
            return redirect(url_for('login_screen'))
    decorated_function.__name__ = f.__name__
    return decorated_function

# TODO Aqui vai entrar a Página de Agendamento
@app.route('/')
def home():
    # return render_template('home.html', text=session['text'])
    # Aqui vai entrar a 
    return '<p>Helloooooo, World!</p>'

# Rota para a página de login
@app.route('/login', methods=['GET', 'POST'])
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
        error_message = session['flash_text']['wait_before_retry'].format(wait_seconds)
        flash(error_message, 'error')
        return render_template('login_screen.html', text=session['text'], error=error_message)

    if request.method == 'POST':
        # Aqui você captura os dados do formulário e envia para a API Java
        email = request.form['email_login']
        senha = request.form['senha_login']
        
        try:
            response = requests.post(app.api + 'usuarios/login', json={'email': email, 'senha': senha})
            if not response.ok:
                session['tries'] += 1
                session['last_try_time'] = datetime.now(ZoneInfo("UTC")).isoformat()
                if session['tries'] >= max_tries:
                    error_message = session['flash_text']['max_attempts'].format(wait_time)
                else:
                    error_message = session['flash_text']['login_failed']
                flash(error_message, 'error')  
                return redirect(url_for('login_screen'))

            elif response.ok:
                # Reseta as tentativas após login bem-sucedido
                session.pop('tries', None)
                session.pop('last_try_time', None)
                session['user'] = response.json() 
                session['logged_in'] = True
                return redirect(url_for('admin'))
            else:
                error_message = session['flash_text']['unknown_error']
                flash(f'{error_message} | Response Code: {response.status_code}', 'error')

                return render_template('login_screen.html', text=session['text'])
        except requests.exceptions.ConnectionError:
            # Captura o caso em que a API está offline ou o hostname não pode ser resolvido
            flash(session['flash_text']['conn_error'], 'error')
        
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
            "senha": request.form['senha_cad'],
            "ativo": True
        }

        # Envia os dados do usuário para a API
        response = requests.post( app.api + 'usuarios/cadastrar', json=user_data)

        if response.status_code == 201:
            # Usuário criado com sucesso, redirecionar para a tela de login ou outra página
            flash(session['flash_text']['create_acc_success'], 'success')
            return redirect(url_for('login_screen'))
        else:
            flash(f"{session['flash_text']['create_acc_fail']} | Response Code: {response.status_code}", 'error')


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
        data_nasc_formatted = datetime.strptime(data_nasc, "%Y-%m-%d").strftime("%Y-%m-%d")

        response = requests.post(app.api + 'usuarios/recuperar-senha', json={'email': email, 'cpf': cpf, 'dataNasc': data_nasc_formatted})
        if response.ok:
            return redirect(url_for('change_password', cpf=cpf))
        else:
            error_message = session['flash_text']['form_dont_match']
            flash(error_message, 'error')

    return render_template('recover_password.html', text=session['text'])

@app.route('/change_password/<cpf>', methods=['GET', 'POST'])
@check_api_status
def change_password(cpf):
    if request.method == 'POST':
        nova_senha = request.form['nova_senha']
        confirma_senha = request.form['confirma_senha']

        if nova_senha != confirma_senha:
            # Senhas não coincidem, exibir mensagem de erro
            error_message = session['text']['passwords_dont_match']
            flash(error_message, 'error')
            return render_template('change_password.html', text=session['text'], cpf=cpf)
        
        # Aqui você chamaria a API para atualizar a senha, por exemplo:
        try:
            response = requests.patch(f'{app.api}usuario/atualizar/{cpf}', json={'cpf': cpf, 'senha': nova_senha})
            if response.ok:
                flash(session['flash_text']['password_changed_success'], 'success')
                return redirect(url_for('login_screen'))
            else:
                # Tratamento de erros da API
                flash(session['flash_text']['password_change_failed'], 'error')
        except requests.exceptions.RequestException as e:
            flash(str(e), 'error')

    # Para método GET ou se ocorreu algum erro no POST, exibe o formulário novamente
    return render_template('change_password.html', text=session['text'], cpf=cpf)

# TODO Aqui a gente precisa incluir o CRUD para Clinicas, Medicos e Agendamentos. Inserir abas para selecionar qual crud será carregados, vamos estudar as possibilidades

# Rota para a página de agendamento 
@app.route('/create_scheduling', methods=['GET', 'POST'])
def create_scheduling_screen():
    if request.method == 'POST':
        # Captura os dados do formulário

        data_nasc = request.form['data_nasc_cad']
        data_nasc_formatted = datetime.strptime(data_nasc, "%Y-%m-%d").strftime("%Y-%m-%d")

        user_data = {
            "nome": request.form['nome_agen'],
            "email": request.form['email_agen'],
            # "dataNasc": data_nasc_formatted,
            "clinica": request.form['clinica_agen'],
            "medico": request.form['medico_agen'],
            "dataHoraAgendamento": request.form['dataHoraAgendamento_agen'],
            # "senha": request.form['senha_cad'],
            "ativo": True
        }

        # Envia os dados do usuário para a API
        response = requests.post( app.api + 'usuarios/cadastrar', json=user_data)

        if response.status_code == 201:
            # Usuário criado com sucesso, redirecionar para a tela de login ou outra página
            flash(session['flash_text']['create_acc_success'], 'success')
            return redirect(url_for('login_screen'))
        else:
            flash(f"{session['flash_text']['create_acc_fail']} | Response Code: {response.status_code}", 'error')


    # Se o método for GET ou se o cadastro falhar, mostra a tela de cadastro novamente
    return render_template('create_scheduling_screen.html', text=session['text'])

# Rota para a página de administração do site
@app.route('/admin', methods=['GET', 'POST'])
@check_api_status
@login_required
def admin():
    # Renderiza o template, passando os dados dos usuários e as mensagens de flash
    return render_template('admin_screen.html', text=session['text'], flash_text=session.pop('flash_text', None))

def obter_usuarios():
    resposta = requests.get(app.api + 'usuarios')
    if resposta.status_code == 200:
        dados = resposta.json()
        return dados
    else:
        flash(f"{session['flash_text']['conn_error']} | Response Code: {resposta.status_code}", 'error')
        return redirect(url_for('admin'))

@app.route('/api/usuarios')
@check_api_status
@login_required
def dados_api():
    dados = obter_usuarios()

    if isinstance(dados, list):
        return jsonify(dados)
    else:
        flash(session['flash_text']['conn_error'], 'error')
        return redirect(url_for('admin'))


@app.route('/api/criar', methods=['POST'])
@check_api_status
@login_required
def api_criar_usuario():
    data = request.json
    user_data = {
        "nome": data['nome'],
        "email": data['email'],
        "dataNasc": datetime.strptime(data['dataNasc'], "%Y-%m-%d").strftime("%Y-%m-%d"),
        "cpf": data['cpf'],
        "senha": data['senha'],
        "ativo": True
    }

    response = requests.post(app.api + 'usuarios/cadastrar', json=user_data)
    if response.status_code == 201:
        flash(session['flash_text']['create_user_success'], 'success')
    else:
        flash(session['flash_text']['create_user_fail'], 'error')
    return str(response.status_code)


@app.route('/api/atualizar/<cpf>', methods=['PATCH'])
@check_api_status
@login_required
def api_atualizar_usuario(cpf):
    if str(session['user']['cpf']) != str(cpf):
        try:
            user_data = request.json
            response = requests.patch(f'{app.api}usuario/atualizar/{cpf}', json=user_data)

            if response.status_code == 200:
                flash(session['flash_text']['update_user_success'], 'success')
            else:
                flash(session['flash_text']['update_user_fail'], 'error')
            return str(response.status_code)

        except Exception as e:
            flash(session['flash_text']['unknown_error'], 'error')
            return '500'

    else:
        flash(session['flash_text']['self_edit_error'], 'error')
        return '403'


@app.route('/api/remover/<cpf>', methods=['DELETE'])
@check_api_status
@login_required
def api_deletar_usuario(cpf):

    if str(session['user']['cpf']) != str(cpf):
        try:
            response = requests.delete(f'{app.api}usuario/remover/{cpf}')

            if response.status_code == 200:
                flash(session['flash_text']['delete_user_success'], 'success')
            else:
                flash(session['flash_text']['delete_user_fail'], 'error')
            return str(response.status_code)
        except requests.exceptions.RequestException as e:
            flash(session['flash_text']['conn_error'], 'error')
            return '500'
    else:
        flash(session['flash_text']['self_delete_error'], 'error')
        return '403'

@app.route("/logout")
@login_required
def logout():
    del session['logged_in']
    return redirect(url_for('login_screen'))

if __name__ == '__main__':
    app.jinja_env.auto_reload = True
    app.config['TEMPLATES_AUTO_RELOAD'] = True
    app.run(debug=True, host='0.0.0.0')

