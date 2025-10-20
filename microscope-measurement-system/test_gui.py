#!/usr/bin/env python3
"""
Script de teste para verificar se a interface GUI está redimensionável
"""

import sys
import os

# Adiciona o diretório python-vision ao path
sys.path.append(os.path.join(os.path.dirname(__file__), 'python-vision'))

try:
    import tkinter as tk
    from microscope_gui import MicroscopeGUI

    def test_gui():
        print("🧪 Testando interface GUI redimensionável...")

        root = tk.Tk()
        app = MicroscopeGUI(root)

        print("✅ Interface criada com sucesso!")
        print("📏 Tamanho inicial:", root.geometry())
        print("🔧 Redimensionável:", root.resizable())
        print("📐 Tamanho mínimo:", f"{root.minsize()}")

        # Testa redimensionamento programático
        root.after(2000, lambda: root.geometry("1200x800"))
        root.after(4000, lambda: root.geometry("1600x1000"))
        root.after(6000, lambda: root.destroy())

        print("🚀 Iniciando teste de redimensionamento...")
        print("   - 2s: Redimensiona para 1200x800")
        print("   - 4s: Redimensiona para 1600x1000")
        print("   - 6s: Fecha automaticamente")

        root.mainloop()

        print("✅ Teste concluído!")

    if __name__ == "__main__":
        test_gui()

except ImportError as e:
    print(f"❌ Erro de importação: {e}")
    print("💡 Execute a partir do diretório raiz do projeto")
except Exception as e:
    print(f"❌ Erro: {e}")