FROM python:3.11-slim-bookworm
WORKDIR /site

# Copiar apenas requirements.txt primeiro para aproveitar o cache de build
COPY requirements.txt /site

# Instalar dependências
RUN pip install -r requirements.txt

# Copiar o restante dos arquivos da aplicação
COPY . /site

# Definir variáveis de ambiente para o Flask
ENV FLASK_APP=app.py
ENV FLASK_ENV=development

# Comando de inicialização para Flask com auto-reload
CMD ["flask", "run", "--host=0.0.0.0", "--reload", "--debug"]
