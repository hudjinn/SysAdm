from flask import Flask, render_template, request, redirect, url_for
import os
import inspect
import json

app = Flask(__name__, static_url_path='/static')

# Ler arquivo de internacionalização
def read_translation(language):

    file_path = f'modules/locate/{language}.json'
    with open(file_path, 'r', encoding='utf-8') as file:
        translations = json.load(file)

    return translations

# Rota para a página de login
@app.route('/')
def login_screen():
    # Implementar leitura de idioma
    text = read_translation('pt_BR') 
    return render_template('login_screen.html', text=text)

# Rota para a página de cadastro
@app.route('/create_account')
def create_account_screen():
    # Implementar leitura de idioma
    text = read_translation('pt_BR') 
    return render_template('create_acc_screen.html', text=text)


if __name__ == '__main__':
    app.run(debug=True)