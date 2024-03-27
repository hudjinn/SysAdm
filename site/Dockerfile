FROM python:3.11-slim-bookworm
WORKDIR /site
COPY . /site
RUN pip install -r requirements.txt
CMD ["flask", "run", "--host=0.0.0.0"]