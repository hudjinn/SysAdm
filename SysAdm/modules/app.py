from flask import Flask, render_template, request, redirect, url_for
import os

app = Flask(__name__)

# Rota para a página de login
@app.route('/')
def login_screen():
    text = os.path('locale/pt_BR.json')
    return render_template('login_screen.html', text=text)