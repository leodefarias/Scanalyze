"""
Interface Gráfica para Sistema de Micromedição
Este módulo implementa uma interface gráfica usando Tkinter que permite
visualizar o preview em tempo real da câmera, ajustar parâmetros de
processamento e registrar medições com um botão dedicado.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import cv2
from PIL import Image, ImageTk
import threading
import time
from datetime import datetime
from microscope_vision import MicroscopeVision
import os
import json

# Try to import requests, if not available, API features will be disabled
try:
    import requests
    REQUESTS_AVAILABLE = True
except ImportError:
    REQUESTS_AVAILABLE = False
    print("⚠️ Módulo 'requests' não encontrado. API REST desabilitada.")


class MicroscopeGUI:
    """
    Interface gráfica principal para o sistema de micromedição.
    """
    
    def __init__(self, root):
        """
        Inicializa a interface gráfica.
        
        Args:
            root: Janela principal do Tkinter
        """
        self.root = root
        self.root.title("🔬 Sistema de Micromedição - Interface de Captura")

        # Configura geometria inicial responsiva
        self.root.geometry("1400x900")
        self.root.minsize(900, 650)  # Tamanho mínimo confortável

        # Permite redimensionamento completo
        self.root.resizable(True, True)

        # Centraliza a janela na tela
        self.center_window()

        # Configura para que a janela seja redimensionável
        self.root.rowconfigure(0, weight=1)
        self.root.columnconfigure(0, weight=1)
        
        # Sistema de visão computacional
        self.vision = MicroscopeVision()
        
        # Variáveis de controle
        self.is_running = False
        self.current_photo = None
        self.update_thread = None

        # API REST integration
        self.api_base_url = "https://scanalyze-api.agreeableplant-ba923b61.eastus2.azurecontainerapps.io/api"
        self.api_available = False
        self.check_api_connection()
        
        # Variáveis de interface
        self.sample_id_var = tk.StringVar(value=f"SAMPLE_{int(time.time())}")
        self.operator_var = tk.StringVar(value="Operador")
        self.threshold_var = tk.IntVar(value=100)
        self.blur_var = tk.IntVar(value=5)
        self.min_area_var = tk.IntVar(value=50)
        self.max_area_var = tk.IntVar(value=50000)
        self.scale_var = tk.DoubleVar(value=10.0)
        self.camera_var = tk.StringVar()

        # Detecta câmeras disponíveis
        self.available_cameras = MicroscopeVision.detect_available_cameras()
        if self.available_cameras:
            first_camera = list(self.available_cameras.keys())[0]
            self.camera_var.set(str(first_camera))
        else:
            self.camera_var.set("0")
        
        # Status
        self.status_var = tk.StringVar(value="Sistema pronto")
        self.area_pixels_var = tk.StringVar(value="0")
        self.area_um2_var = tk.StringVar(value="0.00")
        
        self.setup_ui()
        
    def setup_ui(self):
        """Configura a interface do usuário."""
        # Frame principal
        main_frame = ttk.Frame(self.root, padding="10")
        main_frame.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        
        # Configura peso das colunas/linhas para redimensionamento
        main_frame.columnconfigure(0, weight=0)  # Painel de controle com largura fixa
        main_frame.columnconfigure(1, weight=1)  # Painel de vídeo expansível
        main_frame.rowconfigure(0, weight=1)     # Linha principal expansível
        
        # Painel de controle (esquerda)
        self.setup_control_panel(main_frame)
        
        # Painel de vídeo (direita)
        self.setup_video_panel(main_frame)
        
        # Barra de status (parte inferior)
        self.setup_status_bar()
        
    def setup_control_panel(self, parent):
        """
        Configura o painel de controle com barra de rolagem.

        Args:
            parent: Widget pai
        """
        # Frame container para a barra de rolagem
        scroll_container = ttk.Frame(parent)
        scroll_container.grid(row=0, column=0, sticky=(tk.W, tk.N, tk.S), padx=(0, 10))
        scroll_container.rowconfigure(0, weight=1)
        scroll_container.columnconfigure(0, weight=1)

        # Canvas e scrollbar - agora com largura adaptável
        canvas = tk.Canvas(scroll_container, width=320, highlightthickness=0)
        scrollbar = ttk.Scrollbar(scroll_container, orient="vertical", command=canvas.yview)
        scrollable_frame = ttk.Frame(canvas)

        scrollable_frame.bind(
            "<Configure>",
            lambda e: canvas.configure(scrollregion=canvas.bbox("all"))
        )

        canvas.create_window((0, 0), window=scrollable_frame, anchor="nw")
        canvas.configure(yscrollcommand=scrollbar.set)

        canvas.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))
        scrollbar.grid(row=0, column=1, sticky=(tk.N, tk.S))

        # Bind mouse wheel para funcionar em toda a área (Linux e Windows)
        def _on_mousewheel(event):
            if event.delta:
                # Windows
                canvas.yview_scroll(int(-1*(event.delta/120)), "units")
            else:
                # Linux
                if event.num == 4:
                    canvas.yview_scroll(-1, "units")
                elif event.num == 5:
                    canvas.yview_scroll(1, "units")

        def _bind_to_mousewheel(event):
            canvas.bind_all("<MouseWheel>", _on_mousewheel)  # Windows
            canvas.bind_all("<Button-4>", _on_mousewheel)    # Linux
            canvas.bind_all("<Button-5>", _on_mousewheel)    # Linux

        def _unbind_from_mousewheel(event):
            canvas.unbind_all("<MouseWheel>")
            canvas.unbind_all("<Button-4>")
            canvas.unbind_all("<Button-5>")

        canvas.bind('<Enter>', _bind_to_mousewheel)
        canvas.bind('<Leave>', _unbind_from_mousewheel)

        # Frame de controles dentro do canvas
        control_frame = ttk.LabelFrame(scrollable_frame, text="Controles", padding="10")
        control_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        # Botões principais
        btn_frame = ttk.Frame(control_frame)
        btn_frame.grid(row=0, column=0, columnspan=2, pady=(0, 10), sticky=(tk.W, tk.E))
        
        self.start_btn = ttk.Button(btn_frame, text="Iniciar Câmera", 
                                   command=self.start_camera, style="Success.TButton")
        self.start_btn.pack(side=tk.LEFT, padx=(0, 5))
        
        self.stop_btn = ttk.Button(btn_frame, text="Parar Câmera",
                                  command=self.stop_camera, state=tk.DISABLED)
        self.stop_btn.pack(side=tk.LEFT, padx=(0, 5))

        # Seletor de câmera
        camera_frame = ttk.LabelFrame(control_frame, text="Seleção de Câmera", padding="10")
        camera_frame.grid(row=1, column=0, columnspan=2, pady=10, sticky=(tk.W, tk.E))

        ttk.Label(camera_frame, text="Câmera:").grid(row=0, column=0, sticky=tk.W, pady=2)

        # Combobox com câmeras disponíveis
        camera_values = [f"{idx} - {desc}" for idx, desc in self.available_cameras.items()]
        self.camera_combo = ttk.Combobox(camera_frame, values=camera_values, state="readonly", width=30)
        self.camera_combo.grid(row=0, column=1, pady=2, padx=(5, 0), sticky=(tk.W, tk.E))

        if camera_values:
            self.camera_combo.set(camera_values[0])

        # Botão para trocar câmera
        self.change_camera_btn = ttk.Button(camera_frame, text="Trocar Câmera",
                                           command=self.change_camera)
        self.change_camera_btn.grid(row=0, column=2, padx=(5, 0))

        # Botão para redetectar câmeras
        self.refresh_cameras_btn = ttk.Button(camera_frame, text="🔄 Atualizar",
                                             command=self.refresh_cameras)
        self.refresh_cameras_btn.grid(row=1, column=1, pady=(5, 0))

        # Botão de registro de medição (destaque)
        register_frame = ttk.LabelFrame(control_frame, text="Registro de Medição", padding="10")
        register_frame.grid(row=2, column=0, columnspan=2, pady=10, sticky=(tk.W, tk.E))
        
        # Campos de entrada
        ttk.Label(register_frame, text="ID da Amostra:").grid(row=0, column=0, sticky=tk.W, pady=2)
        ttk.Entry(register_frame, textvariable=self.sample_id_var, width=20).grid(row=0, column=1, pady=2)
        
        ttk.Label(register_frame, text="Operador:").grid(row=1, column=0, sticky=tk.W, pady=2)
        ttk.Entry(register_frame, textvariable=self.operator_var, width=20).grid(row=1, column=1, pady=2)
        
        # Botão de registro (grande e destacado)
        self.register_btn = ttk.Button(register_frame, text="🔬 REGISTRAR MEDIÇÃO", 
                                      command=self.register_measurement,
                                      state=tk.DISABLED)
        self.register_btn.grid(row=2, column=0, columnspan=2, pady=10, sticky=(tk.W, tk.E))
        
        # Informações da medição atual
        info_frame = ttk.LabelFrame(control_frame, text="Medição Atual", padding="10")
        info_frame.grid(row=3, column=0, columnspan=2, pady=10, sticky=(tk.W, tk.E))

        ttk.Label(info_frame, text="Área (pixels):").grid(row=0, column=0, sticky=tk.W)
        ttk.Label(info_frame, textvariable=self.area_pixels_var,
                 font=("Arial", 10, "bold")).grid(row=0, column=1, sticky=tk.W)

        ttk.Label(info_frame, text="Área (μm²):").grid(row=1, column=0, sticky=tk.W)
        ttk.Label(info_frame, textvariable=self.area_um2_var,
                 font=("Arial", 10, "bold")).grid(row=1, column=1, sticky=tk.W)

        # Parâmetros de processamento
        params_frame = ttk.LabelFrame(control_frame, text="Parâmetros de Processamento", padding="10")
        params_frame.grid(row=4, column=0, columnspan=2, pady=10, sticky=(tk.W, tk.E))
        
        # Threshold
        ttk.Label(params_frame, text="Threshold:").grid(row=0, column=0, sticky=tk.W)
        threshold_scale = ttk.Scale(params_frame, from_=0, to=255, 
                                   variable=self.threshold_var, orient=tk.HORIZONTAL,
                                   command=self.update_parameters)
        threshold_scale.grid(row=0, column=1, sticky=(tk.W, tk.E))
        ttk.Label(params_frame, textvariable=self.threshold_var).grid(row=0, column=2)
        
        # Blur
        ttk.Label(params_frame, text="Blur:").grid(row=1, column=0, sticky=tk.W)
        blur_scale = ttk.Scale(params_frame, from_=1, to=15, 
                              variable=self.blur_var, orient=tk.HORIZONTAL,
                              command=self.update_parameters)
        blur_scale.grid(row=1, column=1, sticky=(tk.W, tk.E))
        ttk.Label(params_frame, textvariable=self.blur_var).grid(row=1, column=2)
        
        # Área mínima
        ttk.Label(params_frame, text="Área mín:").grid(row=2, column=0, sticky=tk.W)
        min_area_scale = ttk.Scale(params_frame, from_=10, to=1000, 
                                  variable=self.min_area_var, orient=tk.HORIZONTAL,
                                  command=self.update_parameters)
        min_area_scale.grid(row=2, column=1, sticky=(tk.W, tk.E))
        ttk.Label(params_frame, textvariable=self.min_area_var).grid(row=2, column=2)
        
        # Escala
        ttk.Label(params_frame, text="Escala (px/μm):").grid(row=3, column=0, sticky=tk.W)
        scale_scale = ttk.Scale(params_frame, from_=1.0, to=50.0, 
                               variable=self.scale_var, orient=tk.HORIZONTAL,
                               command=self.update_parameters)
        scale_scale.grid(row=3, column=1, sticky=(tk.W, tk.E))
        ttk.Label(params_frame, text=f"{self.scale_var.get():.1f}").grid(row=3, column=2)
        
        # Botões de utilidade
        utils_frame = ttk.LabelFrame(control_frame, text="Utilitários", padding="10")
        utils_frame.grid(row=5, column=0, columnspan=2, pady=10, sticky=(tk.W, tk.E))
        
        ttk.Button(utils_frame, text="Abrir Pasta de Dados", 
                  command=self.open_data_folder).pack(fill=tk.X, pady=2)
        ttk.Button(utils_frame, text="Resetar Parâmetros", 
                  command=self.reset_parameters).pack(fill=tk.X, pady=2)
        
    def setup_video_panel(self, parent):
        """
        Configura o painel de vídeo redimensionável.

        Args:
            parent: Widget pai
        """
        video_frame = ttk.LabelFrame(parent, text="Preview da Câmera", padding="10")
        video_frame.grid(row=0, column=1, sticky=(tk.W, tk.E, tk.N, tk.S))

        # Configura o frame para ser redimensionável
        video_frame.rowconfigure(0, weight=1)
        video_frame.columnconfigure(0, weight=1)

        # Canvas para exibir o vídeo - agora redimensionável
        self.video_canvas = tk.Canvas(video_frame, bg="black", highlightthickness=0)
        self.video_canvas.grid(row=0, column=0, sticky=(tk.W, tk.E, tk.N, tk.S))

        # Label de status do vídeo
        self.video_status = ttk.Label(video_frame, text="Câmera não iniciada",
                                     font=("Arial", 12))
        self.video_status.grid(row=1, column=0, pady=5, sticky=(tk.W, tk.E))

        # Bind do evento de redimensionamento para ajustar o vídeo
        self.video_canvas.bind('<Configure>', self.on_canvas_resize)

        # Variáveis para controle de redimensionamento
        self.canvas_width = 800
        self.canvas_height = 600
        
    def setup_status_bar(self):
        """Configura a barra de status."""
        status_frame = ttk.Frame(self.root)
        status_frame.grid(row=1, column=0, sticky=(tk.W, tk.E), padx=10, pady=(0, 10))
        
        ttk.Label(status_frame, text="Status:").pack(side=tk.LEFT)
        ttk.Label(status_frame, textvariable=self.status_var).pack(side=tk.LEFT, padx=(5, 0))
        
        # Indicador de gravação
        self.recording_indicator = ttk.Label(status_frame, text="●",
                                           foreground="red", font=("Arial", 20))
        self.recording_indicator.pack(side=tk.RIGHT)
        self.recording_indicator.pack_forget()  # Inicialmente oculto

        # Indicador de API
        api_status_text = "API ✅" if self.api_available else "API ❌"
        api_color = "green" if self.api_available else "red"
        self.api_indicator = ttk.Label(status_frame, text=api_status_text,
                                     foreground=api_color, font=("Arial", 10, "bold"))
        self.api_indicator.pack(side=tk.RIGHT, padx=(10, 5))

    def center_window(self):
        """Centraliza a janela na tela."""
        self.root.update_idletasks()
        width = self.root.winfo_width()
        height = self.root.winfo_height()
        x = (self.root.winfo_screenwidth() // 2) - (width // 2)
        y = (self.root.winfo_screenheight() // 2) - (height // 2)
        self.root.geometry(f"{width}x{height}+{x}+{y}")

    def on_canvas_resize(self, event):
        """
        Callback chamado quando o canvas é redimensionado.
        Atualiza as dimensões para ajustar o vídeo.
        """
        self.canvas_width = event.width
        self.canvas_height = event.height

    def _get_selected_camera_index(self) -> int:
        """
        Obtém o índice da câmera selecionada no combobox.

        Returns:
            int: Índice da câmera selecionada (padrão 0 se inválido)
        """
        try:
            camera_text = self.camera_combo.get()
            if camera_text:
                # Extrai o índice do formato "0 - Câmera..."
                camera_index = int(camera_text.split(' - ')[0])
                return camera_index
        except Exception as e:
            print(f"Erro ao obter índice da câmera: {e}")

        # Fallback para câmera 0
        return 0

    def start_camera(self):
        """Inicia a câmera e o processamento de vídeo."""
        # Obtém o índice da câmera selecionada
        camera_index = self._get_selected_camera_index()

        if self.vision.initialize_camera(camera_index):
            self.is_running = True
            self.start_btn.config(state=tk.DISABLED)
            self.stop_btn.config(state=tk.NORMAL)
            self.register_btn.config(state=tk.NORMAL)

            # Inicia thread de atualização de vídeo
            self.update_thread = threading.Thread(target=self.video_update_loop, daemon=True)
            self.update_thread.start()

            self.status_var.set("Câmera ativa - Preview em tempo real")
            self.video_status.config(text="Câmera ativa - Processando...")
            self.recording_indicator.pack(side=tk.RIGHT)
        else:
            messagebox.showerror("Erro", "Não foi possível inicializar a câmera")
            
    def stop_camera(self):
        """Para a câmera e o processamento de vídeo."""
        self.is_running = False
        
        if self.update_thread:
            self.update_thread.join(timeout=1.0)
            
        self.vision.release_camera()
        
        self.start_btn.config(state=tk.NORMAL)
        self.stop_btn.config(state=tk.DISABLED)
        self.register_btn.config(state=tk.DISABLED)
        
        self.status_var.set("Câmera parada")
        self.video_status.config(text="Câmera parada")
        self.recording_indicator.pack_forget()
        
        # Limpa canvas
        self.video_canvas.delete("all")
        
    def video_update_loop(self):
        """Loop principal de atualização de vídeo."""
        while self.is_running:
            try:
                # Captura e processa frame
                if self.vision.capture_frame():
                    processed_frame, area_um2, area_pixels = self.vision.process_frame()
                    
                    if processed_frame is not None:
                        # Atualiza informações na interface
                        self.root.after(0, self.update_video_display, processed_frame)
                        self.root.after(0, self.update_measurement_info, area_pixels, area_um2)
                
                time.sleep(0.033)  # ~30 FPS
                
            except Exception as e:
                self.root.after(0, lambda: self.status_var.set(f"Erro: {e}"))
                break
                
    def update_video_display(self, frame):
        """
        Atualiza a exibição de vídeo no canvas.
        
        Args:
            frame: Frame processado do OpenCV
        """
        # Redimensiona frame para caber no canvas
        canvas_width = self.video_canvas.winfo_width()
        canvas_height = self.video_canvas.winfo_height()
        
        if canvas_width > 1 and canvas_height > 1:
            # Calcula proporção mantendo aspect ratio
            frame_height, frame_width = frame.shape[:2]
            scale = min(canvas_width / frame_width, canvas_height / frame_height)
            
            new_width = int(frame_width * scale)
            new_height = int(frame_height * scale)
            
            # Redimensiona frame
            resized_frame = cv2.resize(frame, (new_width, new_height))
            
            # Converte BGR para RGB
            rgb_frame = cv2.cvtColor(resized_frame, cv2.COLOR_BGR2RGB)
            
            # Converte para PIL Image e depois para PhotoImage
            pil_image = Image.fromarray(rgb_frame)
            self.current_photo = ImageTk.PhotoImage(pil_image)
            
            # Atualiza canvas
            self.video_canvas.delete("all")
            x = (canvas_width - new_width) // 2
            y = (canvas_height - new_height) // 2
            self.video_canvas.create_image(x, y, anchor=tk.NW, image=self.current_photo)
            
    def update_measurement_info(self, area_pixels, area_um2):
        """
        Atualiza as informações de medição na interface.
        
        Args:
            area_pixels: Área em pixels
            area_um2: Área em micrômetros quadrados
        """
        self.area_pixels_var.set(str(area_pixels))
        self.area_um2_var.set(f"{area_um2:.2f}")
        
    def update_parameters(self, *args):
        """Atualiza os parâmetros de processamento."""
        if hasattr(self, 'vision'):
            self.vision.adjust_parameters(
                blur_kernel=int(self.blur_var.get()),
                threshold=int(self.threshold_var.get()),
                min_area=int(self.min_area_var.get()),
                max_area=int(self.max_area_var.get()),
                scale=float(self.scale_var.get())
            )
            
    def register_measurement(self):
        """Registra a medição atual."""
        if not self.is_running:
            messagebox.showwarning("Aviso", "A câmera deve estar ativa para registrar medições")
            return

        try:
            sample_id = self.sample_id_var.get().strip()
            operator = self.operator_var.get().strip()

            if not sample_id:
                messagebox.showwarning("Aviso", "ID da amostra é obrigatório")
                return

            if not operator:
                operator = "Sistema"

            # Registra a medição localmente (sempre salva em arquivos)
            measurement_data = self.vision.save_measurement(sample_id, operator)

            if measurement_data:
                # Tenta registrar via API se disponível
                api_success = False
                if self.api_available:
                    # Primeiro verifica se a amostra existe, senão cria automaticamente
                    try:
                        response = requests.get(f"{self.api_base_url}/samples/{sample_id}", timeout=5)
                        if response.status_code != 200 or not response.json().get('found'):
                            # Amostra não existe, cria automaticamente
                            self.create_sample_via_api(sample_id, f"Amostra {sample_id}", "Captura GUI", operator)
                    except Exception:
                        # Se der erro, cria amostra mesmo assim
                        self.create_sample_via_api(sample_id, f"Amostra {sample_id}", "Captura GUI", operator)

                    # Registra a medição via API
                    api_success = self.create_measurement_via_api(
                        measurement_data['id'],
                        measurement_data['sampleId'],
                        measurement_data['area_um2'],
                        measurement_data['imagemId']
                    )

                area_display = f"{measurement_data['area_um2']:.4f}"
                pixels_display = f"{measurement_data['area_pixels']:.2f}"

                # Mostra confirmação
                api_status = "✅ Salvo automaticamente na API" if api_success else "⚠️ Salvo apenas em arquivos"
                message = f"""Medição registrada com sucesso!

ID: {measurement_data['id']}
Amostra: {measurement_data['sampleId']}
Área: {area_display} μm² ({pixels_display} pixels)
Operador: {measurement_data['operator']}
Imagem: {measurement_data['nomeImagem']}

{api_status}
Dados salvos em CSV e JSON."""

                messagebox.showinfo("Medição Registrada", message)

                # Atualiza ID da amostra para próxima medição
                self.sample_id_var.set(f"SAMPLE_{int(time.time())}")

                status_msg = f"Última medição: {measurement_data['id']}"
                if api_success:
                    status_msg += " (API ✅)"
                else:
                    status_msg += " (Arquivo)"
                self.status_var.set(status_msg)
            else:
                if self.vision.current_area_pixels <= 0 or self.vision.current_area_um2 <= 0:
                    messagebox.showwarning("Nenhum Contorno Detectado",
                                            "Nenhuma região válida foi encontrada no frame atual. Ajuste os parâmetros e tente novamente.")
                else:
                    messagebox.showerror("Erro", "Falha ao registrar medição")

        except Exception as e:
            messagebox.showerror("Erro", f"Erro ao registrar medição: {e}")
            
    def reset_parameters(self):
        """Reseta os parâmetros para valores padrão."""
        self.threshold_var.set(100)
        self.blur_var.set(5)
        self.min_area_var.set(100)
        self.max_area_var.set(50000)
        self.scale_var.set(10.0)
        self.update_parameters()
        self.status_var.set("Parâmetros resetados")
        
    def open_data_folder(self):
        """Abre a pasta onde os dados são salvos."""
        data_folder = os.path.abspath(self.vision.output_dir)
        try:
            if os.name == 'nt':  # Windows
                os.startfile(data_folder)
            elif os.name == 'posix':  # Linux/Mac
                os.system(f'xdg-open "{data_folder}"')
        except Exception as e:
            messagebox.showinfo("Pasta de Dados", f"Dados salvos em: {data_folder}")
            
    def change_camera(self):
        """Troca para a câmera selecionada."""
        if not self.camera_combo.get():
            messagebox.showwarning("Aviso", "Selecione uma câmera")
            return

        try:
            # Extrai o índice da câmera da seleção
            camera_text = self.camera_combo.get()
            camera_index = int(camera_text.split(' - ')[0])

            # Se a câmera está funcionando, para primeiro
            was_running = self.is_running
            if was_running:
                self.stop_camera()

            # Troca a câmera
            success = self.vision.change_camera(camera_index)

            if success:
                self.status_var.set(f"Câmera trocada para índice {camera_index}")
                messagebox.showinfo("Sucesso", f"Câmera trocada para: {camera_text}")

                # Se estava funcionando, reinicia
                if was_running:
                    self.start_camera()
            else:
                self.status_var.set(f"Falha ao trocar câmera {camera_index}")
                messagebox.showerror("Erro", f"Não foi possível trocar para a câmera {camera_index}")

        except Exception as e:
            messagebox.showerror("Erro", f"Erro ao trocar câmera: {e}")

    def refresh_cameras(self):
        """Redetecta câmeras disponíveis e atualiza a lista."""
        self.status_var.set("Detectando câmeras...")

        try:
            # Redetecta câmeras
            self.available_cameras = MicroscopeVision.detect_available_cameras()

            # Atualiza combobox
            camera_values = [f"{idx} - {desc}" for idx, desc in self.available_cameras.items()]
            self.camera_combo['values'] = camera_values

            if camera_values:
                # Mantém seleção atual se ainda existir, senão seleciona a primeira
                current_camera = self.vision.camera_index
                current_text = f"{current_camera} - {self.available_cameras.get(current_camera, 'Desconhecida')}"

                if current_text in camera_values:
                    self.camera_combo.set(current_text)
                else:
                    self.camera_combo.set(camera_values[0])

                self.status_var.set(f"Detectadas {len(camera_values)} câmeras")
                messagebox.showinfo("Câmeras Atualizadas", f"Detectadas {len(camera_values)} câmeras disponíveis")
            else:
                self.camera_combo.set("")
                self.status_var.set("Nenhuma câmera detectada")
                messagebox.showwarning("Nenhuma Câmera", "Nenhuma câmera foi detectada no sistema")

        except Exception as e:
            messagebox.showerror("Erro", f"Erro ao detectar câmeras: {e}")
            self.status_var.set("Erro na detecção de câmeras")

    def check_api_connection(self):
        """Verifica se a API REST está disponível."""
        if not REQUESTS_AVAILABLE:
            self.api_available = False
            print("⚠️ Módulo 'requests' não disponível - API REST desabilitada")
            return

        try:
            response = requests.get(f"{self.api_base_url}/health", timeout=3)
            if response.status_code == 200:
                self.api_available = True
                print("✅ API REST conectada - salvamento automático habilitado")
            else:
                self.api_available = False
                print("⚠️ API REST indisponível - usando salvamento em arquivos")
        except:
            self.api_available = False
            print("⚠️ API REST indisponível - usando salvamento em arquivos")

    def create_sample_via_api(self, sample_id, nome, tipo, operador_responsavel):
        """Cadastra nova amostra via API REST."""
        if not self.api_available:
            return False

        try:
            data = {
                'id': sample_id,
                'nome': nome,
                'tipo': tipo,
                'operadorResponsavel': operador_responsavel
            }

            response = requests.post(f"{self.api_base_url}/samples", data=data, timeout=10)
            result = response.json()

            if result.get('success'):
                print(f"✅ Amostra {sample_id} cadastrada via API")
                return True
            else:
                print(f"❌ Erro API ao cadastrar amostra: {result.get('message', 'Erro desconhecido')}")
                return False

        except Exception as e:
            print(f"❌ Erro de conexão ao cadastrar amostra via API: {e}")
            return False

    def create_measurement_via_api(self, measurement_id, sample_id, area, imagem_id=None):
        """Registra nova medição via API REST."""
        if not self.api_available:
            return False

        try:
            data = {
                'id': measurement_id,
                'sampleId': sample_id,
                'area': str(area)
            }

            if imagem_id:
                data['imagemId'] = imagem_id

            response = requests.post(f"{self.api_base_url}/measurements", data=data, timeout=10)
            result = response.json()

            if result.get('success'):
                print(f"✅ Medição {measurement_id} registrada via API (Área: {area:.2f} μm²)")
                return True
            else:
                print(f"❌ Erro API ao registrar medição: {result.get('message', 'Erro desconhecido')}")
                return False

        except Exception as e:
            print(f"❌ Erro de conexão ao registrar medição via API: {e}")
            return False

    def on_closing(self):
        """Chamado quando a janela está sendo fechada."""
        if self.is_running:
            self.stop_camera()
        self.root.destroy()


def main():
    """Função principal da interface gráfica."""
    root = tk.Tk()
    
    # Configura estilo
    style = ttk.Style()
    style.theme_use('clam')
    
    # Configura cores personalizadas
    style.configure('Success.TButton', background='#4CAF50')
    style.map('Success.TButton', background=[('active', '#45a049')])
    
    # Cria aplicação
    app = MicroscopeGUI(root)
    
    # Configura evento de fechamento
    root.protocol("WM_DELETE_WINDOW", app.on_closing)
    
    # Centraliza janela
    root.update_idletasks()
    x = (root.winfo_screenwidth() // 2) - (root.winfo_width() // 2)
    y = (root.winfo_screenheight() // 2) - (root.winfo_height() // 2)
    root.geometry(f"+{x}+{y}")
    
    print("=== INTERFACE GRÁFICA DO SISTEMA DE MICROMEDIÇÃO ===")
    print("Interface iniciada. Use os controles para:")
    print("1. Iniciar a câmera")
    print("2. Ajustar parâmetros de processamento")
    print("3. Registrar medições com o botão dedicado")
    print("4. Visualizar preview em tempo real")
    
    # Inicia loop principal
    root.mainloop()


if __name__ == "__main__":
    main()