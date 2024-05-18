# Dockerfile
FROM python:3.11-slim-bookworm
WORKDIR /site

# Copiar apenas requirements.txt primeiro para aproveitar o cache de build
COPY requirements.txt /site

# Instalar dependências
RUN pip install -r requirements.txt

# Copiar o restante dos arquivos da aplicação
COPY . /site

# Copiar o script de inserção de dados e o arquivo de dados
COPY insert_fake_data.py /site
COPY static/dados_fake.json /site/static
COPY wait-for-it.sh /site
RUN chmod +x /site/wait-for-it.sh

# Definir variáveis de ambiente para o Flask
ENV FLASK_APP=app.py
ENV FLASK_ENV=development

# Comando de inicialização para Flask com auto-reload
CMD ["flask", "run", "--host=0.0.0.0", "--reload", "--debug"]
