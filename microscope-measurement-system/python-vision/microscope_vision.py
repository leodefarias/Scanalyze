"""
Módulo de Visão Computacional para Sistema de Micromedição
Este módulo implementa a captura de vídeo em tempo real, processamento
de imagens para detecção de amostras e cálculo de áreas, com prévia
em tempo real e funcionalidade de registro de medições.

Autor: Sistema de Micromedição
Versão: 1.0
"""

import cv2
import numpy as np
import time
import os
import json
import csv
from datetime import datetime
from typing import Tuple, Optional, Dict, Any
import tkinter as tk
from tkinter import ttk, messagebox, filedialog
from PIL import Image, ImageTk
import threading
from oracle_integration import OracleIntegration


class MicroscopeVision:
    """
    Classe principal para visão computacional do microscópio.
    Gerencia captura de vídeo, processamento de imagens e cálculo de áreas.
    """

    @staticmethod
    def detect_available_cameras() -> Dict[int, str]:
        """
        Detecta câmeras disponíveis no sistema.

        Returns:
            Dict[int, str]: Dicionário com índice da câmera e descrição
        """
        available_cameras = {}

        print("🔍 Detectando câmeras disponíveis...")

        # Testa até 10 índices de câmera
        for i in range(10):
            cap = cv2.VideoCapture(i)
            if cap.isOpened():
                # Tenta capturar um frame para verificar se funciona
                ret, frame = cap.read()
                if ret and frame is not None:
                    # Obtém informações da câmera
                    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
                    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
                    fps = int(cap.get(cv2.CAP_PROP_FPS))

                    description = f"Câmera {i} - {width}x{height}@{fps}fps"
                    available_cameras[i] = description
                    print(f"✅ {description}")
                else:
                    print(f"❌ Câmera {i} - Não conseguiu capturar frame")
                cap.release()
            else:
                # Não mostra erro para índices sem câmera para evitar spam
                pass

        if not available_cameras:
            print("❌ Nenhuma câmera detectada!")
            # Adiciona câmera padrão como fallback
            available_cameras[0] = "Câmera Padrão (pode não funcionar)"

        print(f"📷 Total de câmeras detectadas: {len(available_cameras)}")
        return available_cameras

    def __init__(self, camera_index: int = 0, scale_pixels_per_um: float = 10.0):
        """
        Inicializa o sistema de visão computacional.
        
        Args:
            camera_index: Índice da câmera (0 para câmera padrão)
            scale_pixels_per_um: Escala de conversão pixels/micrômetro
        """
        self.camera_index = camera_index
        self.scale_pixels_per_um = scale_pixels_per_um
        self.cap = None
        self.current_frame = None
        self.processed_frame = None
        self.current_area_pixels = 0
        self.current_area_um2 = 0.0
        self.is_running = False
        
        # Parâmetros de processamento de imagem
        self.blur_kernel_size = 5
        self.threshold_value = 120
        self.min_contour_area = 100  # Permite detectar áreas menores
        self.max_contour_area = 999999999900  # Máximo alinhado com limite do banco Oracle (NUMBER(15,6))
        self.debug_mode = False  # Para mostrar imagens intermediárias
        
        # Configurações de arquivo - usa caminho absoluto
        base_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
        self.output_dir = os.path.join(base_dir, "data-integration")
        self.ensure_output_directory()

        # Integração com banco de dados Oracle
        self.oracle_integration = OracleIntegration()
        self.db_connected = False
        self._initialize_database_connection()
        
    def ensure_output_directory(self):
        """Garante que o diretório de saída existe."""
        if not os.path.exists(self.output_dir):
            os.makedirs(self.output_dir)

    def _initialize_database_connection(self):
        """Inicializa a conexão com o banco de dados Oracle."""
        try:
            print("🔗 Tentando conectar ao banco Oracle...")
            self.db_connected = self.oracle_integration.connect()
            if self.db_connected:
                print("✅ Conexão Oracle estabelecida - Registros serão salvos no banco")
            else:
                print("⚠️ Usando modo JSON fallback - Registros serão salvos em arquivos")
        except Exception as e:
            print(f"❌ Erro na inicialização do Oracle: {e}")
            print("⚠️ Usando modo JSON fallback")
            self.db_connected = False
            
    def initialize_camera(self, camera_index: int = None) -> bool:
        """
        Inicializa a câmera.

        Args:
            camera_index: Índice da câmera (opcional, usa self.camera_index se None)

        Returns:
            bool: True se a câmera foi inicializada com sucesso
        """
        try:
            # Se um índice foi fornecido, atualiza o índice atual
            if camera_index is not None:
                self.camera_index = camera_index

            self.cap = cv2.VideoCapture(self.camera_index)
            if not self.cap.isOpened():
                print(f"Erro: Não foi possível abrir a câmera {self.camera_index}")
                return False

            # Configura resolução se possível
            self.cap.set(cv2.CAP_PROP_FRAME_WIDTH, 1280)
            self.cap.set(cv2.CAP_PROP_FRAME_HEIGHT, 720)

            print(f"Câmera inicializada com sucesso (index: {self.camera_index})")
            return True

        except Exception as e:
            print(f"Erro ao inicializar câmera: {e}")
            return False
    
    def release_camera(self):
        """Libera os recursos da câmera."""
        if self.cap:
            self.cap.release()
            self.cap = None

    def release_resources(self):
        """Libera todos os recursos (câmera e banco de dados)."""
        self.release_camera()
        if hasattr(self, 'oracle_integration') and self.oracle_integration:
            self.oracle_integration.disconnect()
            print("🔒 Conexão Oracle finalizada")

    def change_camera(self, new_camera_index: int) -> bool:
        """
        Troca para uma nova câmera.

        Args:
            new_camera_index: Índice da nova câmera

        Returns:
            bool: True se a troca foi bem-sucedida
        """
        # Libera câmera atual
        self.release_camera()

        # Tenta inicializar nova câmera
        self.camera_index = new_camera_index
        success = self.initialize_camera()

        if success:
            print(f"✅ Câmera trocada com sucesso para índice {new_camera_index}")
        else:
            print(f"❌ Falha ao trocar para câmera {new_camera_index}")

        return success

    def get_camera_info(self) -> Dict[str, Any]:
        """
        Obtém informações da câmera atual.

        Returns:
            Dict com informações da câmera
        """
        if not self.cap or not self.cap.isOpened():
            return {"status": "Desconectada", "index": self.camera_index}

        width = int(self.cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(self.cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        fps = int(self.cap.get(cv2.CAP_PROP_FPS))

        return {
            "status": "Conectada",
            "index": self.camera_index,
            "resolution": f"{width}x{height}",
            "fps": fps,
            "description": f"Câmera {self.camera_index} - {width}x{height}@{fps}fps"
        }
    
    def capture_frame(self) -> bool:
        """
        Captura um frame da câmera.
        
        Returns:
            bool: True se o frame foi capturado com sucesso
        """
        if not self.cap:
            return False
            
        ret, frame = self.cap.read()
        if ret:
            self.current_frame = frame.copy()
            return True
        return False
    
    def process_frame(self) -> Tuple[np.ndarray, float, int]:
        """
        Processa o frame atual para detectar e medir amostras.
        
        Returns:
            Tuple[np.ndarray, float, int]: Frame processado, área em μm², área em pixels
        """
        if self.current_frame is None:
            return None, 0.0, 0
        
        # Cria uma cópia do frame para processamento
        frame = self.current_frame.copy()
        
        # Converte para escala de cinza
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        
        # Aplica filtro Gaussiano para reduzir ruído
        blurred = cv2.GaussianBlur(gray, (self.blur_kernel_size, self.blur_kernel_size), 0)
        
        # Aplica threshold para binarizar a imagem (THRESH_BINARY_INV para objetos escuros)
        _, thresh = cv2.threshold(blurred, self.threshold_value, 255, cv2.THRESH_BINARY_INV)
        
        # Aplica operações morfológicas suaves para preservar formas irregulares
        kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2, 2))
        thresh = cv2.morphologyEx(thresh, cv2.MORPH_CLOSE, kernel)  # Fecha pequenos buracos apenas
        
        # Encontra contornos
        contours, _ = cv2.findContours(thresh, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
        
        # Filtra contornos por área
        valid_contours = []
        total_area_pixels = 0
        
        for contour in contours:
            area = cv2.contourArea(contour)
            if self.min_contour_area <= area <= self.max_contour_area:
                valid_contours.append(contour)
                total_area_pixels += area
        
        # Desenha contornos no frame original
        cv2.drawContours(frame, valid_contours, -1, (0, 255, 0), 2)
        
        # Adiciona pontos centrais dos contornos para melhor visualização
        for contour in valid_contours:
            M = cv2.moments(contour)
            if M["m00"] != 0:
                cx = int(M["m10"] / M["m00"])
                cy = int(M["m01"] / M["m00"])
                cv2.circle(frame, (cx, cy), 5, (0, 255, 0), -1)
        
        # Calcula área total em micrômetros quadrados
        total_area_um2 = total_area_pixels / (self.scale_pixels_per_um ** 2)
        
        # Debug removido - sem janelas extras
        
        # Adiciona informações de texto no frame
        self._add_info_text(frame, total_area_pixels, total_area_um2, len(valid_contours))
        
        # Atualiza valores atuais
        self.current_area_pixels = total_area_pixels
        self.current_area_um2 = total_area_um2
        self.processed_frame = frame
        
        return frame, total_area_um2, total_area_pixels
    
    def _add_info_text(self, frame: np.ndarray, area_pixels: int, area_um2: float, num_contours: int):
        """
        Adiciona informações de texto no frame.
        
        Args:
            frame: Frame onde adicionar o texto
            area_pixels: Área em pixels
            area_um2: Área em micrômetros quadrados
            num_contours: Número de contornos detectados
        """
        font = cv2.FONT_HERSHEY_SIMPLEX
        font_scale = 0.6
        color = (255, 255, 255)
        thickness = 2
        
        # Fundo semi-transparente para o texto
        overlay = frame.copy()
        cv2.rectangle(overlay, (10, 10), (400, 120), (0, 0, 0), -1)
        cv2.addWeighted(overlay, 0.7, frame, 0.3, 0, frame)
        
        # Adiciona textos informativos
        cv2.putText(frame, f"Contornos detectados: {num_contours}", (15, 30), font, font_scale, color, thickness)
        cv2.putText(frame, f"Area total: {area_pixels} pixels", (15, 55), font, font_scale, color, thickness)
        cv2.putText(frame, f"Area total: {area_um2:.2f} um2", (15, 80), font, font_scale, color, thickness)
        cv2.putText(frame, f"Escala: {self.scale_pixels_per_um:.1f} px/um", (15, 105), font, font_scale, color, thickness)
    
    def save_measurement(self, sample_id: str = "AUTO", operator: str = "Sistema") -> Dict[str, Any]:
        """
        Salva a medição atual em arquivos CSV e JSON.
        
        Args:
            sample_id: ID da amostra
            operator: Nome do operador
            
        Returns:
            Dict com informações da medição salva
        """
        if self.current_frame is None or self.processed_frame is None:
            return None

        if self.current_area_pixels <= 0 or self.current_area_um2 <= 0:
            print("⚠️ Nenhum contorno válido foi detectado. Medição não será registrada.")
            return None

        timestamp = datetime.now()
        measurement_id = f"MEAS_{int(timestamp.timestamp())}"
        image_id = f"IMG_{int(timestamp.timestamp())}"
        image_filename = f"{image_id}.jpg"
        image_path = os.path.join(self.output_dir, image_filename)

        # Converte a imagem original para bytes (para salvar no banco)
        success, buffer = cv2.imencode('.jpg', self.current_frame)
        image_bytes = buffer.tobytes() if success else None

        # Salva a imagem original localmente (como backup)
        cv2.imwrite(image_path, self.current_frame)

        # Salva também a imagem processada para referência local
        processed_filename = f"{image_id}_processed.jpg"
        processed_path = os.path.join(self.output_dir, processed_filename)
        cv2.imwrite(processed_path, self.processed_frame)
        
        area_pixels_value = float(self.current_area_pixels)
        area_um2_value = float(self.current_area_um2)
        rounded_area_um2 = round(area_um2_value, 6)
        if rounded_area_um2 <= 0:
            rounded_area_um2 = 0.000001

        # Cria dados da medição
        measurement_data = {
            "id": measurement_id,
            "sampleId": sample_id,
            "area_pixels": area_pixels_value,
            "area_um2": rounded_area_um2,
            "dataHora": timestamp.strftime("%Y-%m-%d %H:%M:%S"),
            "imagemId": image_id,
            "nomeImagem": image_filename,
            "imagemBytes": image_bytes,  # Adiciona os bytes da imagem
            "operator": operator,
            "scale_pixels_per_um": self.scale_pixels_per_um
        }
        
        # Salva no banco Oracle (prioridade) ou JSON (fallback)
        db_success = False
        if self.db_connected:
            print("💾 Salvando no banco Oracle...")
            db_success = self.oracle_integration.insert_measurement(measurement_data)

        if not db_success:
            print("💾 Salvando em arquivos locais...")
            # Salva em CSV
            self._save_to_csv(measurement_data)

            # Salva em JSON
            self._save_to_json(measurement_data)

        status_msg = "salva no banco Oracle" if db_success else "salva em arquivos locais"
        print(f"✅ Medição {status_msg}: {measurement_id}")
        print(f"📏 Área: {rounded_area_um2:.6f} μm² ({area_pixels_value:.2f} pixels)")
        print(f"🖼️ Imagem: {image_filename}")

        return measurement_data
    
    def _save_to_csv(self, measurement_data: Dict[str, Any]):
        """
        Salva medição em arquivo CSV.

        Args:
            measurement_data: Dados da medição
        """
        csv_path = os.path.join(self.output_dir, "measurements.csv")
        file_exists = os.path.exists(csv_path)

        # Remove imagemBytes para evitar erro de serialização em CSV
        csv_data = {k: v for k, v in measurement_data.items() if k != 'imagemBytes'}

        with open(csv_path, 'a', newline='', encoding='utf-8') as csvfile:
            fieldnames = ['id', 'sampleId', 'area_pixels', 'area_um2', 'dataHora',
                         'imagemId', 'nomeImagem', 'operator', 'scale_pixels_per_um']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

            # Escreve cabeçalho se o arquivo não existir
            if not file_exists:
                writer.writeheader()

            writer.writerow(csv_data)
    
    def _save_to_json(self, measurement_data: Dict[str, Any]):
        """
        Salva medição em arquivo JSON.

        Args:
            measurement_data: Dados da medição
        """
        json_path = os.path.join(self.output_dir, "measurements.json")

        # Remove imagemBytes para evitar erro de serialização em JSON
        json_data = {k: v for k, v in measurement_data.items() if k != 'imagemBytes'}

        # Carrega dados existentes ou cria lista vazia
        measurements = []
        if os.path.exists(json_path):
            try:
                with open(json_path, 'r', encoding='utf-8') as jsonfile:
                    data = json.load(jsonfile)
                    measurements = data.get('measurements', [])
            except json.JSONDecodeError:
                measurements = []

        # Adiciona nova medição
        measurements.append(json_data)

        # Salva dados atualizados
        with open(json_path, 'w', encoding='utf-8') as jsonfile:
            json.dump({"measurements": measurements}, jsonfile, indent=2, ensure_ascii=False)
    
    def adjust_parameters(self, blur_kernel: int = None, threshold: int = None, 
                         min_area: int = None, max_area: int = None, scale: float = None):
        """
        Ajusta parâmetros de processamento.
        
        Args:
            blur_kernel: Tamanho do kernel de blur
            threshold: Valor de threshold
            min_area: Área mínima de contorno
            max_area: Área máxima de contorno
            scale: Escala pixels/micrômetro
        """
        if blur_kernel is not None:
            self.blur_kernel_size = max(1, blur_kernel if blur_kernel % 2 == 1 else blur_kernel + 1)
        if threshold is not None:
            self.threshold_value = max(0, min(255, threshold))
        if min_area is not None:
            self.min_contour_area = max(1, min_area)
        if max_area is not None:
            self.max_contour_area = max(self.min_contour_area, max_area)
        if scale is not None:
            self.scale_pixels_per_um = max(0.1, scale)
    
    def toggle_debug_mode(self):
        """Alterna o modo de debug."""
        self.debug_mode = not self.debug_mode
        if not self.debug_mode:
            cv2.destroyWindow('Debug - Threshold')
            cv2.destroyWindow('Debug - Gray')
        print(f"Modo debug: {'ativado' if self.debug_mode else 'desativado'}")
    
    def get_measurement_info(self) -> Dict[str, Any]:
        """
        Retorna informações da medição atual.
        
        Returns:
            Dict com informações da medição atual
        """
        return {
            "area_pixels": self.current_area_pixels,
            "area_um2": round(self.current_area_um2, 2),
            "scale": self.scale_pixels_per_um,
            "parameters": {
                "blur_kernel": self.blur_kernel_size,
                "threshold": self.threshold_value,
                "min_area": self.min_contour_area,
                "max_area": self.max_contour_area
            }
        }


def main():
    """Função principal para teste do módulo de visão computacional."""
    print("=== SISTEMA DE VISÃO COMPUTACIONAL ===")
    print("Inicializando módulo de micromedição...")
    
    # Cria instância do sistema de visão
    vision = MicroscopeVision()
    
    # Inicializa câmera
    if not vision.initialize_camera():
        print("Erro: Não foi possível inicializar a câmera")
        return
    
    print("Câmera inicializada. Pressione:")
    print("- ESPAÇO: Registrar medição")
    print("- 's': Salvar medição atual")
    print("- 'q': Sair")
    print("- '+'/'-': Ajustar threshold")
    print("- 'd': Alternar modo debug")
    
    try:
        while True:
            # Captura frame
            if not vision.capture_frame():
                print("Erro ao capturar frame")
                break
            
            # Processa frame
            processed_frame, area_um2, area_pixels = vision.process_frame()
            
            if processed_frame is not None:
                # Mostra frame processado
                cv2.imshow('Microscope Vision - Real Time', processed_frame)
            
            # Processa teclas
            key = cv2.waitKey(1) & 0xFF
            
            if key == ord('q'):
                break
            elif key == ord('s') or key == ord(' '):
                measurement = vision.save_measurement(f"SAMPLE_{int(time.time())}")
                if measurement:
                    print(f"Medição registrada: {measurement['id']}")
            elif key == ord('+') or key == ord('='):
                vision.adjust_parameters(threshold=vision.threshold_value + 5)
                print(f"Threshold ajustado para: {vision.threshold_value}")
            elif key == ord('-'):
                vision.adjust_parameters(threshold=vision.threshold_value - 5)
                print(f"Threshold ajustado para: {vision.threshold_value}")
            elif key == ord('d'):
                vision.toggle_debug_mode()
    
    except KeyboardInterrupt:
        print("\nInterrompido pelo usuário")
    
    finally:
        # Limpa recursos
        vision.release_resources()
        cv2.destroyAllWindows()
        print("🏁 Sistema finalizado")


if __name__ == "__main__":
    main()
