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

# Rota inicial para a página de agendamento 
@app.route('/', methods=['GET', 'POST'])
def create_scheduling_screen():
    crud = request.args.get('crud', 'usuarios')  # Define 'usuarios' como padrão se nenhum CRUD for especificado

    if request.method == 'POST':
        data = request.form
        data_hora_agendamento = f"{data['data_agen']}T{data['hora_agen']}"  # Ajuste no formato da data e hora

        agendamento_data = {
            "nomePaciente": data['nome_agen'],
            "emailPaciente": data['email_agen'],
            "clinica": {"id": data['clinica_agen']},
            "medico": {"id": data['medico_agen']},
            "dataHoraAgendamento": data_hora_agendamento,
        }
        response = requests.post(app.api + 'agendamentos', json=agendamento_data)
        if response.status_code == 201:
            protocolo_id = response.json().get('id')  # Extrair o ID do agendamento da resposta
            flash(f"Agendamento criado com sucesso!\nProtocolo: {protocolo_id}", 'success')
        else:
            flash("Erro ao criar agendamento", 'error')
        
    return render_template('create_scheduling_screen.html', crud=crud, text=session['text'])


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

@app.route("/logout")
@login_required
def logout():
    del session['logged_in']
    return redirect(url_for('login_screen'))



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

# Rota para a página de administração do site
@app.route('/admin', methods=['GET', 'POST'])
@check_api_status
#@login_required
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

# --------------API----------------

# --------------ADMIN--------------
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


@app.route('/api/usuarios/criar', methods=['POST'])
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
        return jsonify({"message": session['flash_text']['create_user_success']}), 201
    elif response.status_code == 409:
        return jsonify({"message": "CPF já cadastrado"}), 409
    else:
        return jsonify({"message": session['flash_text']['create_user_fail']}), response.status_code


@app.route('/api/usuarios/atualizar/<cpf>', methods=['PATCH'])
@check_api_status
@login_required
def api_atualizar_usuario(cpf):
    if str(session['user']['cpf']) != str(cpf):
        try:
            user_data = request.json
            user_data["cpf"] = cpf  # Adicione o CPF no corpo da requisição

            response = requests.patch(f'{app.api}usuarios/atualizar/{cpf}', json=user_data)

            if response.status_code == 200:
                return jsonify({"message": session['flash_text']['update_user_success']}), 200
            else:
                return jsonify({"message": session['flash_text']['update_user_fail']}), response.status_code

        except Exception as e:
            return jsonify({"message": session['flash_text']['unknown_error']}), 500

    else:
        return jsonify({"message": session['flash_text']['self_edit_error']}), 403

@app.route('/api/usuarios/remover/<cpf>', methods=['DELETE'])
@check_api_status
@login_required
def api_deletar_usuario(cpf):
    if str(session['user']['cpf']) != str(cpf):
        try:
            response = requests.delete(f'{app.api}usuarios/remover/{cpf}')

            if response.status_code == 200:
                return jsonify({"message": session['flash_text']['delete_user_success']}), 200
            else:
                return jsonify({"message": session['flash_text']['delete_user_fail']}), response.status_code
        except requests.exceptions.RequestException as e:
            return jsonify({"message": session['flash_text']['conn_error']}), 500
    else:
        return jsonify({"message": session['flash_text']['self_delete_error']}), 403

    
# ---------------MEDICO CRUD--------------------
# Rota para obter todos os médicos
@app.route('/api/medicos')
@check_api_status
@login_required
def dados_medicos():
    try:
        resposta = requests.get(app.api + 'medicos')
        if resposta.status_code == 200:
            dados = resposta.json()
            return jsonify(dados)
        else:
            flash(f"{session['flash_text']['conn_error']} | Response Code: {resposta.status_code}", 'error')
            return redirect(url_for('admin'))
    except requests.exceptions.RequestException as e:
        flash(f"{session['flash_text']['conn_error']} | Error: {e}", 'error')
        return redirect(url_for('admin'))

@app.route('/api/medicos/cadastrar', methods=['POST'])
@check_api_status
@login_required
def api_criar_medico():
    data = request.json
    medico_data = {
        "cpf": data['cpf'],
        "nome": data['nome'],
        "especialidade": data['especialidade'],
    }

    response = requests.post(app.api + 'medicos/cadastrar', json=medico_data)
    if response.status_code == 201:
        return jsonify({"message": session['flash_text']['create_user_success']}), 201
    elif response.status_code == 409:
        return jsonify({"message": "CPF já cadastrado"}), 409
    else:
        return jsonify({"message": session['flash_text']['create_user_fail']}), response.status_code

@app.route('/api/medicos/atualizar/<id>', methods=['PATCH'])
@check_api_status
@login_required
def api_atualizar_medico(id):
    try:
        medico_data = request.json
        response = requests.patch(f'{app.api}medicos/atualizar/{id}', json=medico_data)
        if response.status_code == 200:
            return jsonify({"message": session['flash_text']['update_user_success']}), 200
        else:
            return jsonify({"message": session['flash_text']['update_user_fail']}), response.status_code
    except Exception as e:
        return jsonify({"message": session['flash_text']['unknown_error']}), 500

@app.route('/api/medicos/remover/<id>', methods=['DELETE'])
@check_api_status
@login_required
def api_deletar_medico(id):
    try:
        response = requests.delete(f'{app.api}medicos/remover/{id}')
        if response.status_code == 200:
            return jsonify({"message": session['flash_text']['delete_user_success']}), 200
        else:
            return jsonify({"message": session['flash_text']['delete_user_fail']}), response.status_code
    except requests.exceptions.RequestException as e:
        return jsonify({"message": session['flash_text']['conn_error']}), 500



# -----------------CLINICAS CRUD---------------------
# Rota para obter todas as clínicas
@app.route('/api/clinicas')
@check_api_status
def dados_clinicas():
    try:
        resposta = requests.get(app.api + 'clinicas')
        if resposta.status_code == 200:
            dados = resposta.json()
            return jsonify(dados)
        else:
            flash(f"{session['flash_text']['conn_error']} | Response Code: {resposta.status_code}", 'error')
            return redirect(url_for('admin'))
    except requests.exceptions.RequestException as e:
        flash(f"{session['flash_text']['conn_error']} | Error: {e}", 'error')
        return redirect(url_for('admin'))

@app.route('/api/clinicas/cadastrar', methods=['POST'])
@check_api_status
@login_required
def api_criar_clinica():
    data = request.json
    clinica_data = {
        "nome": data['nome'],
        "endereco": data['endereco']
    }
    
    response = requests.post(app.api + 'clinicas/cadastrar', json=clinica_data)
    if response.status_code == 201:
        return jsonify({"message": session['flash_text']['create_clinic_success']}), 201
    elif response.status_code == 409:
        return jsonify({"message": "Nome da clínica já cadastrado"}), 409
    else:
        return jsonify({"message": session['flash_text']['create_clinic_fail']}), response.status_code

@app.route('/api/clinicas/atualizar/<int:id>', methods=['PATCH'])
@check_api_status
@login_required
def api_atualizar_clinica(id):
    try:
        clinica_data = request.json
        response = requests.patch(f'{app.api}clinicas/atualizar/{id}', json=clinica_data)
        if response.status_code == 200:
            return jsonify({"message": session['flash_text']['update_clinic_success']}), 200
        else:
            return jsonify({"message": session['flash_text']['update_clinic_fail']}), response.status_code
    except Exception as e:
        return jsonify({"message": session['flash_text']['unknown_error']}), 500

@app.route('/api/clinicas/remover/<int:id>', methods=['DELETE'])
@check_api_status
@login_required
def api_deletar_clinica(id):
    try:
        response = requests.delete(f'{app.api}clinicas/remover/{id}')
        if response.status_code == 200:
            return jsonify({"message": session['flash_text']['delete_clinic_success']}), 200
        else:
            return jsonify({"message": session['flash_text']['delete_clinic_fail']}), response.status_code
    except requests.exceptions.RequestException as e:
        return jsonify({"message": session['flash_text']['conn_error']}), 500

# ------------AGENDAMENTO CRUD--------------------

@app.route('/api/agendamentos/cadastrar', methods=['POST'])
def api_criar_agendamento():
    data = request.json
    agendamento_data = {
        "nomePaciente": data['nomePaciente'],
        "emailPaciente": data['emailPaciente'],
        "clinica": {"id": data['clinica']},
        "medico": {"id": data['medico']},
        "dataHoraAgendamento": data['dataHoraAgendamento'],
        "status": 'pendente'
    }
    response = requests.post(app.api + 'agendamentos', json=agendamento_data)
    if response.status_code == 201:
        protocolo_id = response.json().get('id')  # Extrair o ID do agendamento da resposta
        return jsonify({"message": f"Agendamento criado com sucesso!\nProtocolo: {protocolo_id}"}), 201
    else:
        return jsonify({"message": "Erro ao criar agendamento"}), response.status_code

@app.route('/api/agendamentos/atualizar/<id>', methods=['PATCH'])
def api_atualizar_agendamento(id):
    try:
        agendamento_data = request.json
        response = requests.patch(f'{app.api}agendamentos/atualizar/{id}', json=agendamento_data)
        if response.status_code == 200:
            return jsonify({"message": "Agendamento atualizado com sucesso!"}), 200
        else:
            return jsonify({"message": "Erro ao atualizar agendamento"}), response.status_code
    except Exception as e:
        return jsonify({"message": "Erro desconhecido ao atualizar agendamento"}), 500

@app.route('/api/agendamentos/remover/<id>', methods=['DELETE'])
def api_deletar_agendamento(id):
    try:
        response = requests.delete(f'{app.api}agendamentos/remover/{id}')
        if response.status_code == 200:
            return jsonify({"message": "Agendamento removido com sucesso!"}), 200
        else:
            return jsonify({"message": "Erro ao remover agendamento"}), response.status_code
    except requests.exceptions.RequestException as e:
        return jsonify({"message": "Erro ao conectar com o servidor"}), 500

# ---------- CLINICAS CONTROLE -----------
# Rota para obter especialidades de uma clínica
@app.route('/api/clinicas/<int:clinica_id>/especialidades')
@check_api_status

def obter_especialidades(clinica_id):
    try:
        resposta = requests.get(app.api + f'clinicas/{clinica_id}/especialidades')
        if resposta.status_code == 200:
            dados = resposta.json()
            return jsonify(dados)
        else:
            flash(f"{session['flash_text']['conn_error']} | Response Code: {resposta.status_code}", 'error')
            return redirect(url_for('admin'))
    except requests.exceptions.RequestException as e:
        flash(f"{session['flash_text']['conn_error']} | Error: {e}", 'error')
        return redirect(url_for('admin'))

# Rota para obter médicos por especialidade de uma clínica
@app.route('/api/clinicas/<int:clinica_id>/especialidades/<string:especialidade>/medicos')
@check_api_status

def obter_medicos_por_especialidade(clinica_id, especialidade):
    try:
        resposta = requests.get(app.api + f'clinicas/{clinica_id}/especialidades/{especialidade}/medicos')
        if resposta.status_code == 200:
            dados = resposta.json()
            return jsonify(dados)
        else:
            flash(f"{session['flash_text']['conn_error']} | Response Code: {resposta.status_code}", 'error')
            return redirect(url_for('admin'))
    except requests.exceptions.RequestException as e:
        flash(f"{session['flash_text']['conn_error']} | Error: {e}", 'error')
        return redirect(url_for('admin'))

# ------------AGENDAMENTO CONTROLE ------------
@app.route('/api/agendamentos/')
def dados_agendamentos():
    try:
        resposta = requests.get(app.api + 'agendamentos')
        if resposta.status_code == 200:
            dados = resposta.json()

            return jsonify(dados)
        else:
            flash(f"{session['flash_text']['conn_error']} | Response Code: {resposta.status_code}", 'error')
    except requests.exceptions.RequestException as e:
        flash(f"{session['flash_text']['conn_error']} | Error: {e}", 'error')

@app.route('/api/agendamentos/dias-disponiveis')
def get_dias_disponiveis():
    clinica_id = request.args.get('clinicaId')
    medico_id = request.args.get('medicoId')
    
    response = requests.get(f'{app.api}agendamentos/dias-disponiveis?clinicaId={clinica_id}&medicoId={medico_id}')
    if response.status_code == 200:
        return jsonify(response.json())
    else:
        return jsonify({'error': 'Erro ao buscar dias disponíveis'}), response.status_code

@app.route('/api/agendamentos/horarios-disponiveis')
def get_horarios_disponiveis():
    clinica_id = request.args.get('clinicaId')
    medico_id = request.args.get('medicoId')
    dia = request.args.get('dia')

    if clinica_id and medico_id and dia:
        response = requests.get(f"{app.api}agendamentos/horarios-disponiveis?clinicaId={clinica_id}&medicoId={medico_id}&dia={dia}")
        if response.status_code == 200:
            return jsonify(response.json())
        else:
            return jsonify({'error': 'Erro ao buscar horários disponíveis'}), response.status_code
    else:
        return jsonify({'error': 'Parâmetros insuficientes'}), 400


if __name__ == '__main__':
    app.jinja_env.auto_reload = True
    app.config['TEMPLATES_AUTO_RELOAD'] = True
    app.run(debug=True, host='0.0.0.0')
