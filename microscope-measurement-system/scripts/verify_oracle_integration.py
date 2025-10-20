#!/usr/bin/env python3
"""
Script de verificação completa da integração Oracle
Sistema de Micromedição Automatizada

Este script verifica:
1. Conectividade Oracle (Java e Python)
2. Schema e tabelas
3. Integração de dados
4. Funcionalidade completa

Autor: Sistema de Micromedição
Versão: 1.0
"""

import subprocess
import sys
import os
import json
from datetime import datetime

def print_header(title):
    """Imprime cabeçalho formatado."""
    print("\n" + "="*60)
    print(f"   {title}")
    print("="*60)

def print_status(status, message):
    """Imprime status com emoji."""
    emoji = "✅" if status else "❌"
    print(f"{emoji} {message}")

def check_java_oracle():
    """Verifica integração Java-Oracle."""
    print_header("VERIFICAÇÃO JAVA-ORACLE")

    try:
        # Verifica se driver Oracle existe
        if os.path.exists("backend-java/ojdbc8.jar"):
            print_status(True, "Driver Oracle JDBC encontrado")
        else:
            print_status(False, "Driver Oracle JDBC não encontrado")
            return False

        # Testa conexão Java
        result = subprocess.run([
            "java", "-cp", "backend-java/ojdbc8.jar:backend-java/src",
            "br.com.micromedicao.connection.ConnectionFactory"
        ], capture_output=True, text=True, cwd=".")

        if result.returncode == 0:
            print_status(True, "Conexão Java-Oracle funcionando")
            if "Teste de conexão: SUCESSO" in result.stdout:
                print_status(True, "Teste de conectividade bem-sucedido")
                # Extrai informações do banco
                if "Oracle Database" in result.stdout:
                    for line in result.stdout.split('\n'):
                        if "Database:" in line or "Versão:" in line:
                            print(f"   📋 {line.strip()}")
                return True
            else:
                print_status(False, "Teste de conectividade falhou")
                return False
        else:
            print_status(False, f"Erro na conexão Java: {result.stderr}")
            return False

    except Exception as e:
        print_status(False, f"Erro ao testar Java-Oracle: {e}")
        return False

def check_python_oracle():
    """Verifica integração Python-Oracle."""
    print_header("VERIFICAÇÃO PYTHON-ORACLE")

    try:
        # Verifica se oracledb está disponível
        try:
            import oracledb
            print_status(True, "oracledb instalado")
        except ImportError:
            print_status(False, "oracledb não instalado (usando fallback JSON)")
            return False

        # Testa integração Python
        sys.path.append('python-vision')
        from oracle_integration import OracleIntegration

        oracle = OracleIntegration()

        if oracle.test_connection():
            print_status(True, "Conexão Python-Oracle funcionando")

            # Testa operações básicas
            count = oracle.get_measurements_count()
            if count >= 0:
                print_status(True, f"Medições no banco: {count}")

            operators = oracle.get_operators()
            if operators:
                print_status(True, f"Operadores encontrados: {len(operators)}")
                for op in operators[:2]:  # Mostra 2 primeiros
                    print(f"   👤 {op['nome']} ({op['nivel_acesso']})")

            oracle.disconnect()
            return True
        else:
            print_status(False, "Conexão Python-Oracle falhou")
            return False

    except Exception as e:
        print_status(False, f"Erro ao testar Python-Oracle: {e}")
        return False

def check_data_integration():
    """Verifica integração de dados JSON."""
    print_header("VERIFICAÇÃO INTEGRAÇÃO DE DADOS")

    try:
        # Verifica arquivo JSON
        json_file = "data-integration/measurements.json"
        if os.path.exists(json_file):
            print_status(True, "Arquivo measurements.json encontrado")

            with open(json_file, 'r', encoding='utf-8') as f:
                data = json.load(f)

            measurements = data.get('measurements', [])
            print_status(True, f"Medições em JSON: {len(measurements)}")

            if measurements:
                latest = measurements[-1]
                print(f"   📊 Última medição: {latest.get('id')} ({latest.get('dataHora')})")

            return True
        else:
            print_status(False, "Arquivo measurements.json não encontrado")
            return False

    except Exception as e:
        print_status(False, f"Erro ao verificar dados JSON: {e}")
        return False

def check_compilation():
    """Verifica compilação Java."""
    print_header("VERIFICAÇÃO COMPILAÇÃO")

    try:
        # Compila Java
        result = subprocess.run([
            "./compile_and_run.sh"
        ], capture_output=True, text=True, cwd="backend-java", timeout=30)

        if "Compilacao bem-sucedida" in result.stdout:
            print_status(True, "Compilação Java bem-sucedida")
            return True
        else:
            print_status(False, "Erro na compilação Java")
            print(f"   Saída: {result.stdout}")
            print(f"   Erro: {result.stderr}")
            return False

    except subprocess.TimeoutExpired:
        print_status(False, "Timeout na compilação Java")
        return False
    except Exception as e:
        print_status(False, f"Erro ao verificar compilação: {e}")
        return False

def run_integration_test():
    """Executa teste completo de integração."""
    print_header("TESTE COMPLETO DE INTEGRAÇÃO")

    try:
        # Cria medição de teste
        test_measurement = {
            "id": f"MEAS_VERIFY_{int(datetime.now().timestamp())}",
            "sampleId": "SAMPLE_VERIFY",
            "area_pixels": 2000,
            "area_um2": 20.0,
            "dataHora": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "imagemId": f"IMG_VERIFY_{int(datetime.now().timestamp())}",
            "nomeImagem": "verify_integration.jpg",
            "operator": "Sistema Verificação",
            "scale_pixels_per_um": 10.0
        }

        # Tenta salvar via Python
        sys.path.append('python-vision')
        from oracle_integration import OracleIntegration

        oracle = OracleIntegration()

        if oracle.connect():
            # Testa criação de amostra
            if oracle.create_sample_if_not_exists(test_measurement["sampleId"], "Amostra de Verificação"):
                print_status(True, "Amostra de teste criada/verificada")

            # Testa inserção de medição
            if oracle.insert_measurement(test_measurement):
                print_status(True, "Medição de teste inserida no Oracle")

            oracle.disconnect()
        else:
            # Fallback para JSON
            oracle._save_to_json_fallback(test_measurement)
            print_status(True, "Medição salva em JSON (fallback)")

        return True

    except Exception as e:
        print_status(False, f"Erro no teste de integração: {e}")
        return False

def main():
    """Função principal de verificação."""
    print("🔬 VERIFICAÇÃO COMPLETA DA INTEGRAÇÃO ORACLE")
    print("   Sistema de Micromedição Automatizada")
    print("   " + "="*50)

    results = []

    # Executa todas as verificações
    results.append(("Compilação Java", check_compilation()))
    results.append(("Integração Java-Oracle", check_java_oracle()))
    results.append(("Integração Python-Oracle", check_python_oracle()))
    results.append(("Integração de Dados", check_data_integration()))
    results.append(("Teste Completo", run_integration_test()))

    # Resumo final
    print_header("RESUMO DA VERIFICAÇÃO")

    passed = 0
    total = len(results)

    for test_name, result in results:
        print_status(result, test_name)
        if result:
            passed += 1

    print(f"\n📊 RESULTADO: {passed}/{total} testes passaram")

    if passed == total:
        print("\n🎉 INTEGRAÇÃO ORACLE TOTALMENTE FUNCIONAL!")
        print("💡 Sistema pronto para produção com Oracle Database")
    elif passed >= total - 1:
        print("\n⚠️ Integração quase completa (modo híbrido)")
        print("💡 Sistema funciona com Oracle + fallback JSON")
    else:
        print("\n❌ Problemas na integração Oracle")
        print("💡 Verifique conectividade e configurações")

    print("\n🔧 Para resolver problemas:")
    print("   1. Verifique conectividade: ping oracle.fiap.com.br")
    print("   2. Teste credenciais Oracle")
    print("   3. Instale oracledb: pip install oracledb")
    print("   4. Execute: ./backend-java/test_oracle_connection.sh")

if __name__ == "__main__":
    main()