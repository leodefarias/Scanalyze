#!/usr/bin/env python3
"""
Script de Verificação Completa do Setup Oracle
Sistema de Micromedição Automatizada

Este script verifica se o Oracle Database está configurado corretamente
e diagnostica problemas comuns do sistema.

Autor: Sistema de Micromedição
Versão: 2.0
Data: 2025
"""

import sys
import os
import json
from datetime import datetime
from typing import Dict, Any

# Adiciona o diretório python-vision ao path
sys.path.append(os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), 'python-vision'))

try:
    from oracle_integration import OracleIntegration, install_oracledb
except ImportError as e:
    print(f"❌ Erro ao importar oracle_integration: {e}")
    print("💡 Certifique-se de que está executando do diretório correto")
    sys.exit(1)

def print_header():
    """Imprime cabeçalho do script."""
    print("=" * 70)
    print("  🔬 VERIFICAÇÃO COMPLETA DO ORACLE DATABASE")
    print("     Sistema de Micromedição Automatizada")
    print("=" * 70)
    print()

def print_section(title: str):
    """Imprime título de seção."""
    print(f"\n{'─' * 50}")
    print(f"📋 {title}")
    print('─' * 50)

def print_result(status: bool, message: str):
    """Imprime resultado com emoji apropriado."""
    emoji = "✅" if status else "❌"
    print(f"{emoji} {message}")

def print_warning(message: str):
    """Imprime mensagem de aviso."""
    print(f"⚠️  {message}")

def print_info(message: str):
    """Imprime mensagem informativa."""
    print(f"💡 {message}")

def check_dependencies():
    """Verifica dependências do sistema."""
    print_section("VERIFICAÇÃO DE DEPENDÊNCIAS")

    # Verificar Python
    python_version = f"{sys.version_info.major}.{sys.version_info.minor}.{sys.version_info.micro}"
    print_result(True, f"Python versão: {python_version}")

    # Verificar oracledb
    try:
        import oracledb
        print_result(True, "Módulo oracledb disponível")
        return True
    except ImportError:
        print_result(False, "Módulo oracledb não encontrado")
        print_info("Tentando instalar oracledb automaticamente...")

        if install_oracledb():
            print_result(True, "Módulo oracledb instalado com sucesso")
            return True
        else:
            print_result(False, "Falha na instalação automática do oracledb")
            print_info("Execute: pip install oracledb")
            return False

def test_oracle_connection():
    """Testa conexão Oracle e realiza diagnóstico completo."""
    print_section("TESTE DE CONEXÃO ORACLE")

    oracle = OracleIntegration()

    # Diagnóstico completo
    diagnosis = oracle.diagnose_oracle_issues()

    # Conexão
    print_result(diagnosis['connection_ok'], "Conexão Oracle")
    if not diagnosis['connection_ok']:
        print_warning("Não foi possível conectar ao Oracle Database")
        print_info("Configuração: oracle.fiap.com.br:1521/orcl")
        print_info("Usuário: RM555211")

    # Tabelas
    print_result(diagnosis['tables_exist'], "Tabelas do sistema")
    if not diagnosis['tables_exist']:
        print_warning("Uma ou mais tabelas estão faltando")

    # Dados iniciais
    print_result(diagnosis['data_initialized'], "Dados iniciais")
    if not diagnosis['data_initialized']:
        print_warning("Dados iniciais não foram encontrados")

    # Problemas encontrados
    if diagnosis['issues_found']:
        print_section("PROBLEMAS ENCONTRADOS")
        for i, issue in enumerate(diagnosis['issues_found'], 1):
            print(f"{i}. ❌ {issue}")

    # Recomendações
    if diagnosis['recommendations']:
        print_section("RECOMENDAÇÕES")
        for i, recommendation in enumerate(diagnosis['recommendations'], 1):
            print(f"{i}. 💡 {recommendation}")

    return diagnosis

def test_oracle_operations():
    """Testa operações básicas do Oracle."""
    print_section("TESTE DE OPERAÇÕES ORACLE")

    oracle = OracleIntegration()

    if not oracle.test_connection():
        print_result(False, "Operações Oracle não disponíveis")
        return False

    try:
        # Testar contagem de medições
        count = oracle.get_measurements_count()
        if count >= 0:
            print_result(True, f"Contagem de medições: {count}")
        else:
            print_result(False, "Erro ao contar medições")

        # Testar busca de operadores
        operators = oracle.get_operators()
        if operators:
            print_result(True, f"Operadores encontrados: {len(operators)}")
            for op in operators[:3]:  # Mostra apenas os primeiros 3
                print(f"   📋 {op['nome']} ({op['nivel_acesso']})")
        else:
            print_result(False, "Nenhum operador encontrado")

        return True

    except Exception as e:
        print_result(False, f"Erro nas operações Oracle: {e}")
        return False

def test_measurement_insertion():
    """Testa inserção de medição de teste."""
    print_section("TESTE DE INSERÇÃO DE MEDIÇÃO")

    oracle = OracleIntegration()

    # Dados de teste
    test_measurement = {
        "id": f"MEAS_VERIFY_{int(datetime.now().timestamp())}",
        "sampleId": f"SAMPLE_VERIFY_{int(datetime.now().timestamp())}",
        "area_pixels": 15000,
        "area_um2": 150.0,
        "dataHora": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "imagemId": f"IMG_VERIFY_{int(datetime.now().timestamp())}",
        "nomeImagem": "verify_test.jpg",
        "operator": "Sistema Verificação",
        "scale_pixels_per_um": 10.0
    }

    try:
        success = oracle.insert_measurement(test_measurement)
        if success:
            print_result(True, f"Medição de teste inserida: {test_measurement['id']}")
            return True
        else:
            print_result(False, "Falha na inserção da medição de teste")
            return False

    except Exception as e:
        print_result(False, f"Erro ao inserir medição de teste: {e}")
        return False

def generate_report(results: Dict[str, Any]):
    """Gera relatório final da verificação."""
    print_section("RELATÓRIO FINAL")

    all_tests_passed = all([
        results.get('dependencies', False),
        results.get('connection', False),
        results.get('operations', False)
    ])

    if all_tests_passed:
        print("🎉 ORACLE DATABASE: TOTALMENTE FUNCIONAL")
        print("✅ Todos os testes foram aprovados")
        print("✅ Sistema pronto para produção")
    else:
        print("⚠️  ORACLE DATABASE: PROBLEMAS DETECTADOS")
        print("❌ Um ou mais testes falharam")
        print("💡 Consulte as recomendações acima")

    # Salvar relatório em arquivo
    report_data = {
        'timestamp': datetime.now().isoformat(),
        'system_status': 'FUNCTIONAL' if all_tests_passed else 'ISSUES_DETECTED',
        'test_results': results,
        'oracle_diagnosis': results.get('oracle_diagnosis', {})
    }

    report_file = os.path.join(
        os.path.dirname(os.path.abspath(__file__)),
        f"oracle_verification_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    )

    try:
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(report_data, f, indent=2, ensure_ascii=False)
        print(f"📄 Relatório salvo: {report_file}")
    except Exception as e:
        print(f"⚠️  Não foi possível salvar relatório: {e}")

def main():
    """Função principal do script."""
    print_header()

    results = {}

    # 1. Verificar dependências
    results['dependencies'] = check_dependencies()

    # 2. Testar conexão Oracle
    oracle_diagnosis = test_oracle_connection()
    results['oracle_diagnosis'] = oracle_diagnosis
    results['connection'] = oracle_diagnosis['connection_ok'] and oracle_diagnosis['tables_exist']

    # 3. Testar operações (só se conexão OK)
    if results['connection']:
        results['operations'] = test_oracle_operations()

        # 4. Teste de inserção (opcional)
        insertion_test = input("\n🤔 Deseja testar inserção de medição? (s/N): ").strip().lower()
        if insertion_test in ['s', 'sim', 'y', 'yes']:
            results['insertion'] = test_measurement_insertion()
    else:
        results['operations'] = False
        print_warning("Testes de operação pulados devido a problemas de conexão")

    # 5. Relatório final
    generate_report(results)

    # 6. Instruções adicionais
    if not results['connection']:
        print("\n" + "=" * 70)
        print("📋 PASSOS PARA RESOLVER PROBLEMAS:")
        print("=" * 70)
        print("1. Execute o script de setup Oracle:")
        print("   sqlplus RM555211/281005@oracle.fiap.com.br:1521/orcl @scripts/setup_oracle_database.sql")
        print("\n2. Ou conecte-se manualmente e execute:")
        print("   - backend-java/database/oracle_schema.sql")
        print("   - scripts/setup_oracle_database.sql")
        print("\n3. Verifique conectividade de rede:")
        print("   ping oracle.fiap.com.br")
        print("   telnet oracle.fiap.com.br 1521")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⏹️  Verificação interrompida pelo usuário")
        sys.exit(0)
    except Exception as e:
        print(f"\n❌ Erro inesperado: {e}")
        sys.exit(1)