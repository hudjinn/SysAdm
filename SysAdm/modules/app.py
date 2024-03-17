from flask import Flask, render_template, request, redirect, url_for, session
import os
import json

app = Flask(__name__, static_url_path='/static')
text_path = '/home/pi_4_engenharia_software/SysAdm/modules/locate'
app.secret_key = os.urandom(24)

# Simulação de dados de usuários
users = {
    "user1@example.com": {
        "cpf": "12345678900",
        "data_nasc": "1990-01-01"
    }
}

# Ler arquivo de internacionalização
def read_translation(language):
    file_path = f'{text_path}/{language}.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        translations = json.load(file)
    return translations

# Rota para a página de login
@app.route('/')
def login_screen():
    # Implementar leitura de idioma
    text = read_translation('pt_BR') 
    session['text'] = text  # Armazena os textos na sessão
    return render_template('login_screen.html', text=text)

# Rota para a página de cadastro
@app.route('/create_account')
def create_account_screen():
    # Implementar leitura de idioma
    text = read_translation('pt_BR') 
    session['text'] = text  # Armazena os textos na sessão
    return render_template('create_acc_screen.html', text=text)

# Rota para a página de recuperação de senha
@app.route('/recover_password', methods=['GET', 'POST'])
def recover_password():
    text = session.get('text')
    if request.method == 'POST':
        
        email = request.form['email_recovery']
        cpf = request.form['cpf_recovery']
        data_nasc = request.form['data_nasc_recovery']
        # Verificar se os dados fornecidos correspondem a um usuário no banco
        if email in users and users[email]['cpf'] == cpf and users[email]['data_nasc'] == data_nasc:
            # Renderizar a página de alteração de senha, passando o e-mail como parâmetro
            # TODO Consulta no banco
            return redirect(url_for('change_password', email=email))
        else:
            # Se os dados estiverem incorretos, redirecionar de volta para a página de recuperação de senha
            return redirect(url_for('recover_password'))
    else:
        return render_template('recover_password.html', text=text)

# Rota para a página de alteração de senha
@app.route('/change_password/<email>', methods=['GET', 'POST'])
def change_password(email):
    text = read_translation('pt_BR')

    if request.method == 'POST':
        # Processar o formulário de alteração de senha
        nova_senha = request.form['nova_senha']
        confirma_senha = request.form['confirma_senha']
        if nova_senha == confirma_senha:

            return redirect(url_for('password_changed', email=email))

        else:
            # Senhas não coincidem, exibir mensagem de erro
            error_message = "As senhas digitadas não coincidem."
            return render_template('change_password.html', text=text, error_message=error_message, email=email)
    else:
        return render_template('change_password.html', text=text, email=email)

# Rota para a página de sucesso na alteração de senha
@app.route('/password_changed/<email>', methods=['GET', 'POST'])
def password_changed(email):
    text = read_translation('pt_BR')
    return render_template('password_changed.html', text=text, email=email)

# Rota para a página de administração do site
@app.route('/admin_screen')
def admin_screen():
    text = session.get('text')

    return render_template('admin_screen.html', text=text)
    
if __name__ == '__main__':
    app.run(debug=True)
