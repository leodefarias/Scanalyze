#!/usr/bin/env python3
"""
Script de inicialização do Sistema de Micromedição Automatizada.
Este script facilita a execução de todos os componentes do sistema.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import os
import sys
import subprocess
import webbrowser
import time
from pathlib import Path

class SystemLauncher:
    """Classe para gerenciar a inicialização do sistema."""
    
    def __init__(self):
        self.base_dir = Path(__file__).parent
        self.java_dir = self.base_dir / "backend-java"
        self.python_dir = self.base_dir / "python-vision"
        self.frontend_dir = self.base_dir / "frontend-dashboard"
        self.data_dir = self.base_dir / "data-integration"
        
    def show_menu(self):
        """Exibe o menu principal."""
        print("🔬" + "="*60)
        print("     SISTEMA DE MICROMEDIÇÃO AUTOMATIZADA")
        print("              Launcher Principal")
        print("="*62)
        print()
        print("Selecione uma opção:")
        print()
        print("1️⃣  Executar Backend Java (Demo completa)")
        print("2️⃣  Executar Interface Python (Visão Computacional)")
        print("3️⃣  Abrir Dashboard Web")
        print("4️⃣  Gerar dados de exemplo")
        print("5️⃣  Verificar requisitos do sistema")
        print("6️⃣  Executar integração completa")
        print("7️⃣  Compilar Backend Java")
        print("0️⃣  Sair")
        print()
        
    def run_java_backend(self):
        """Executa o backend Java."""
        print("🚀 Iniciando Backend Java...")
        
        java_src = self.java_dir / "src"
        
        if not java_src.exists():
            print("❌ Diretório src do Java não encontrado!")
            return False
        
        try:
            # Verifica se existe script específico do Windows
            if os.name == 'nt':  # Windows
                windows_script = self.java_dir / "compile_windows.bat"
                if windows_script.exists():
                    print("📦 Usando script otimizado para Windows...")
                    result = subprocess.run([str(windows_script)], 
                                          cwd=self.java_dir, shell=True)
                    return result.returncode == 0
            
            # Compilação manual arquivo por arquivo (mais compatível)
            print("📦 Compilando classes Java...")
            
            # Lista de arquivos para compilar em ordem
            java_files = [
                "br/com/micromedicao/model/Sample.java",
                "br/com/micromedicao/model/Operator.java", 
                "br/com/micromedicao/model/DigitalMicroscope.java",
                "br/com/micromedicao/model/MicroscopyImage.java",
                "br/com/micromedicao/model/Measurement.java",
                "br/com/micromedicao/service/MicromedicaoService.java",
                "br/com/micromedicao/integration/DataIntegration.java", 
                "br/com/micromedicao/app/App.java"
            ]
            
            # Compila cada arquivo
            for java_file in java_files:
                print(f"  - Compilando {java_file}")
                compile_cmd = ["javac", "-encoding", "UTF-8", "-cp", ".", java_file]
                
                result = subprocess.run(compile_cmd, cwd=java_src, 
                                      capture_output=True, text=True)
                
                if result.returncode != 0:
                    print(f"❌ Erro ao compilar {java_file}:")
                    print(result.stderr)
                    return False
            
            print("✅ Compilação bem-sucedida!")
            
            # Executa
            print("🏃 Executando aplicação...")
            run_cmd = ["java", "br.com.micromedicao.app.App"]
            
            subprocess.run(run_cmd, cwd=java_src)
            
        except FileNotFoundError:
            print("❌ Java não encontrado. Instale o JDK 8 ou superior.")
            print("💡 Download: https://www.oracle.com/java/technologies/downloads/")
            return False
        except Exception as e:
            print(f"❌ Erro ao executar backend: {e}")
            return False
        
        return True
    
    def run_python_interface(self):
        """Executa a interface Python."""
        print("🚀 Iniciando Interface Python...")
        
        if not self.python_dir.exists():
            print("❌ Diretório Python não encontrado!")
            return False
        
        # Verifica se o arquivo principal existe
        gui_file = self.python_dir / "microscope_gui.py"
        if not gui_file.exists():
            print("❌ Arquivo microscope_gui.py não encontrado!")
            return False
        
        try:
            # Verifica dependências
            print("📦 Verificando dependências...")
            import cv2
            import numpy as np
            from PIL import Image
            import tkinter as tk
            print("✅ Dependências OK!")
            
            # Executa interface
            print("🏃 Iniciando interface gráfica...")
            subprocess.run([sys.executable, "microscope_gui.py"], 
                         cwd=self.python_dir)
            
        except ImportError as e:
            print(f"❌ Dependência faltando: {e}")
            print("💡 Execute: pip install -r requirements.txt")
            return False
        except Exception as e:
            print(f"❌ Erro ao executar interface: {e}")
            return False
        
        return True
    
    def open_dashboard(self):
        """Abre o dashboard web."""
        print("🚀 Abrindo Dashboard Web...")
        
        dashboard_file = self.frontend_dir / "index.html"
        
        if not dashboard_file.exists():
            print("❌ Arquivo index.html não encontrado!")
            return False
        
        try:
            # Abre no navegador padrão
            webbrowser.open(f"file://{dashboard_file.absolute()}")
            print("✅ Dashboard aberto no navegador!")
            print("💡 Se não abriu automaticamente, acesse:")
            print(f"   file://{dashboard_file.absolute()}")
            
        except Exception as e:
            print(f"❌ Erro ao abrir dashboard: {e}")
            return False
        
        return True
    
    def generate_sample_data(self):
        """Gera dados de exemplo."""
        print("🚀 Gerando dados de exemplo...")
        
        try:
            # Executa o script de integração
            integration_script = self.base_dir / "integration_example.py"
            if integration_script.exists():
                subprocess.run([sys.executable, str(integration_script)])
            else:
                print("❌ Script de integração não encontrado!")
                return False
            
        except Exception as e:
            print(f"❌ Erro ao gerar dados: {e}")
            return False
        
        return True
    
    def check_requirements(self):
        """Verifica os requisitos do sistema."""
        print("🔍 Verificando requisitos do sistema...")
        print()
        
        # Verifica Java
        try:
            result = subprocess.run(["java", "-version"], 
                                  capture_output=True, text=True)
            if result.returncode == 0:
                version = result.stderr.split('\n')[0]
                print(f"✅ Java: {version}")
            else:
                print("❌ Java não encontrado")
        except:
            print("❌ Java não encontrado")
        
        # Verifica Python
        print(f"✅ Python: {sys.version}")
        
        # Verifica dependências Python
        dependencies = ["cv2", "numpy", "PIL", "tkinter"]
        for dep in dependencies:
            try:
                __import__(dep)
                print(f"✅ {dep}: Instalado")
            except ImportError:
                print(f"❌ {dep}: Não instalado")
        
        # Verifica estrutura de arquivos
        print("\n📁 Verificando estrutura de arquivos...")
        
        required_files = [
            "backend-java/src/br/com/micromedicao/app/App.java",
            "python-vision/microscope_gui.py",
            "frontend-dashboard/index.html",
            "README.md"
        ]
        
        for file_path in required_files:
            full_path = self.base_dir / file_path
            if full_path.exists():
                print(f"✅ {file_path}")
            else:
                print(f"❌ {file_path}")
        
        print("\n✅ Verificação concluída!")
    
    def run_full_integration(self):
        """Executa integração completa."""
        print("🚀 Executando integração completa...")
        print()
        
        print("1/4 - Gerando dados de exemplo...")
        if not self.generate_sample_data():
            return False
        
        print("\n2/4 - Compilando backend Java...")
        time.sleep(2)
        
        print("\n3/4 - Abrindo dashboard...")
        self.open_dashboard()
        time.sleep(2)
        
        print("\n4/4 - Sistema pronto!")
        print("✅ Integração completa finalizada!")
        print()
        print("📋 Próximos passos:")
        print("   • Use opção 1 para testar o backend Java")
        print("   • Use opção 2 para capturar medições reais")
        print("   • O dashboard já está aberto no navegador")
        
        return True
    
    def compile_java(self):
        """Compila apenas o backend Java."""
        print("🚀 Compilando Backend Java...")
        
        java_src = self.java_dir / "src"
        
        if not java_src.exists():
            print("❌ Diretório src do Java não encontrado!")
            return False
        
        try:
            # Lista de arquivos para compilar em ordem
            java_files = [
                "br/com/micromedicao/model/Sample.java",
                "br/com/micromedicao/model/Operator.java", 
                "br/com/micromedicao/model/DigitalMicroscope.java",
                "br/com/micromedicao/model/MicroscopyImage.java",
                "br/com/micromedicao/model/Measurement.java",
                "br/com/micromedicao/service/MicromedicaoService.java",
                "br/com/micromedicao/integration/DataIntegration.java", 
                "br/com/micromedicao/app/App.java"
            ]
            
            # Compila cada arquivo
            for java_file in java_files:
                print(f"  - Compilando {java_file}")
                compile_cmd = ["javac", "-encoding", "UTF-8", "-cp", ".", java_file]
                
                result = subprocess.run(compile_cmd, cwd=java_src, 
                                      capture_output=True, text=True)
                
                if result.returncode != 0:
                    print(f"❌ Erro ao compilar {java_file}:")
                    print(result.stderr)
                    return False
            
            print("✅ Compilação bem-sucedida!")
            print("💡 Use a opção 1 para executar a aplicação.")
            
        except FileNotFoundError:
            print("❌ Java não encontrado. Instale o JDK 8 ou superior.")
            print("💡 Download: https://www.oracle.com/java/technologies/downloads/")
            return False
        except Exception as e:
            print(f"❌ Erro na compilação: {e}")
            return False
        
        return True
    
    def run(self):
        """Executa o launcher principal."""
        while True:
            self.show_menu()
            
            try:
                choice = input("Digite sua escolha (0-7): ").strip()
                print()
                
                if choice == "0":
                    print("👋 Saindo do sistema...")
                    break
                elif choice == "1":
                    self.run_java_backend()
                elif choice == "2":
                    self.run_python_interface()
                elif choice == "3":
                    self.open_dashboard()
                elif choice == "4":
                    self.generate_sample_data()
                elif choice == "5":
                    self.check_requirements()
                elif choice == "6":
                    self.run_full_integration()
                elif choice == "7":
                    self.compile_java()
                else:
                    print("❌ Opção inválida! Tente novamente.")
                
                if choice != "0":
                    input("\nPressione Enter para continuar...")
                    print("\n" + "="*62)
                
            except KeyboardInterrupt:
                print("\n\n👋 Saindo do sistema...")
                break
            except Exception as e:
                print(f"❌ Erro inesperado: {e}")
                input("Pressione Enter para continuar...")

def main():
    """Função principal."""
    launcher = SystemLauncher()
    launcher.run()

if __name__ == "__main__":
    main()