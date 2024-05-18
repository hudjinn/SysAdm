# insert_fake_data.py
import requests
import json

# URL da API
API_URL = 'http://sysadm-api:8080/'

# Função para carregar dados do arquivo JSON
def load_json(filename):
    with open(filename, 'r') as file:
        return json.load(file)

# Função para inserir dados na API
def insert_data(endpoint, data):
    response = requests.post(endpoint, json=data)
    if response.ok:
        print(f"Sucesso ao inserir dados em {endpoint}")
    else:
        print(f"Erro ao inserir dados em {endpoint}: {response.text}")

def insert_agendamentos(endpoint, agendamentos):
    for agendamento in agendamentos:
        # Certifique-se de que a estrutura do agendamento está correta
        agendamento_data = {
            "clinica": {"id": agendamento["clinicaId"]},
            "medico": {"id": agendamento["medicoId"]},
            "dataAgendamento": agendamento["dataAgendamento"],
            "horaAgendamento": agendamento["horaAgendamento"],
            "status": agendamento["status"],
            "nomePaciente": agendamento["nomePaciente"],
            "emailPaciente": agendamento["emailPaciente"]
        }
        response = requests.post(endpoint, json=agendamento_data)
        if response.status_code == 201:
            print(f"Sucesso ao inserir agendamento: {agendamento_data}")
        else:
            print(f"Erro ao inserir agendamento: {agendamento_data}: {response.text}")

def main():
    data = load_json('static/dados_fake.json')

    usuarios = data['usuarios']
    clinicas = data['clinicas']
    medicos = data['medicos']
    clinica_medico = data['clinica_medico']
    agendamentos = data['agendamentos']
    # Inserir usuários em lote
    insert_data(API_URL + 'usuarios/cadastrar/lote', usuarios)

    # Inserir clínicas em lote
    insert_data(API_URL + 'clinicas/cadastrar/lote', clinicas)

    # Inserir médicos em lote
    insert_data(API_URL + 'medicos/cadastrar/lote', medicos)

    # Inserir horários e associações de médicos a clínicas
    for cm in clinica_medico:
        clinica_id = cm["clinicaId"]
        medico_cpf = cm["medicoCpf"]
        horarios = cm["horarios"]

        # Adicionar médico à clínica
        response = requests.post(f'{API_URL}clinicas/{clinica_id}/medicos/cpf/{medico_cpf}')
        if not response.ok:
            print(f"Erro ao adicionar médico {medico_cpf} à clínica {clinica_id}: {response.text}")
            continue

        # Adicionar horários ao médico na clínica
        for dia, horas in horarios.items():
            data = {
                "diaSemana": dia,
                "horarioInicio": horas[0],
                "horarioFim": horas[1]
            }
            url = f'{API_URL}clinicas/{clinica_id}/medicos/cpf/{medico_cpf}/horarios'
            response = requests.post(url, json=data)
            if response.status_code == 201:
                print(f"Sucesso ao inserir horário para médico {medico_cpf} na clínica {clinica_id}")
            else:
                print(f"Erro ao inserir horário para médico {medico_cpf} na clínica {clinica_id}: {response.text}")
    
    insert_agendamentos(API_URL + 'agendamentos', agendamentos)

if __name__ == '__main__':
    main()
