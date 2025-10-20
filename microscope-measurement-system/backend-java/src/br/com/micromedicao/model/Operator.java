package br.com.micromedicao.model;

import java.util.regex.Pattern;

/**
 * Classe que representa um operador do sistema de micromedição.
 * Esta classe encapsula as informações de um usuário do sistema,
 * incluindo dados pessoais, credenciais e nível de acesso.
 * 
 * @author Sistema de Micromedição
 * @version 1.0
 */
public class Operator {
    
    /**
     * Identificador único do operador
     */
    private String id;
    
    /**
     * Nome completo do operador
     */
    private String nome;
    
    /**
     * Endereço de email do operador
     */
    private String email;
    
    /**
     * Nível de acesso do operador no sistema
     * (ADMIN, TECNICO, OPERADOR)
     */
    private String nivelAcesso;

    /**
     * Construtor completo para criar um novo operador.
     * 
     * @param id Identificador único do operador
     * @param nome Nome completo do operador
     * @param email Endereço de email válido
     * @param nivelAcesso Nível de acesso no sistema
     */
    public Operator(String id, String nome, String email, String nivelAcesso) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.nivelAcesso = nivelAcesso;
    }

    // Getters e Setters

    /**
     * Obtém o identificador do operador.
     * 
     * @return String com o ID do operador
     */
    public String getId() {
        return id;
    }

    /**
     * Define o identificador do operador.
     * 
     * @param id Novo identificador do operador
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Obtém o nome do operador.
     * 
     * @return String com o nome completo
     */
    public String getNome() {
        return nome;
    }

    /**
     * Define o nome do operador.
     * 
     * @param nome Novo nome do operador
     */
    public void setNome(String nome) {
        this.nome = nome;
    }

    /**
     * Obtém o email do operador.
     * 
     * @return String com o endereço de email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Define o email do operador.
     * 
     * @param email Novo endereço de email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Obtém o nível de acesso do operador.
     * 
     * @return String com o nível de acesso
     */
    public String getNivelAcesso() {
        return nivelAcesso;
    }

    /**
     * Define o nível de acesso do operador.
     * 
     * @param nivelAcesso Novo nível de acesso
     */
    public void setNivelAcesso(String nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    // Métodos de funcionalidade

    /**
     * Exibe os detalhes completos do operador no console.
     * Este método formata e apresenta todas as informações do operador
     * de forma legível para o usuário.
     */
    public void exibirDetalhes() {
        System.out.println("=== DETALHES DO OPERADOR ===");
        System.out.println("ID: " + this.id);
        System.out.println("Nome: " + this.nome);
        System.out.println("Email: " + this.email);
        System.out.println("Nível de Acesso: " + this.nivelAcesso);
        System.out.println("Permissões: " + obterDescricaoPermissoes());
        System.out.println("========================");
    }

    /**
     * Autentica o operador no sistema.
     * Simula um processo de autenticação verificando se o operador
     * possui as informações básicas necessárias.
     * 
     * @return boolean true se a autenticação foi bem-sucedida
     */
    public boolean autenticar() {
        boolean emailValido = validarEmail(this.email);
        boolean nomeValido = this.nome != null && !this.nome.trim().isEmpty();
        boolean nivelValido = this.nivelAcesso != null && !this.nivelAcesso.trim().isEmpty();
        
        if (emailValido && nomeValido && nivelValido) {
            System.out.println("Operador " + this.nome + " autenticado com sucesso!");
            return true;
        } else {
            System.out.println("Falha na autenticação do operador " + this.nome);
            return false;
        }
    }

    /**
     * Autentica o operador com credenciais específicas.
     * Sobrecarga do método autenticar para verificar credenciais.
     * 
     * @param emailLogin Email para login
     * @param senha Senha do operador (simulada)
     * @return boolean true se as credenciais estão corretas
     */
    public boolean autenticar(String emailLogin, String senha) {
        boolean emailCorreto = this.email.equals(emailLogin);
        boolean senhaValida = senha != null && senha.length() >= 6;
        
        if (emailCorreto && senhaValida) {
            System.out.println("Login realizado com sucesso para " + this.nome);
            return true;
        } else {
            System.out.println("Credenciais inválidas para " + emailLogin);
            return false;
        }
    }

    /**
     * Verifica se o operador tem permissão para uma operação específica.
     * 
     * @param operacao Tipo de operação a ser verificada
     * @return boolean true se tem permissão
     */
    public boolean temPermissao(String operacao) {
        if (operacao == null) return false;
        
        switch (this.nivelAcesso.toUpperCase()) {
            case "ADMIN":
                return true; // Admin tem acesso a tudo
            case "TECNICO":
                return operacao.equalsIgnoreCase("MEDIR") || 
                       operacao.equalsIgnoreCase("VISUALIZAR") ||
                       operacao.equalsIgnoreCase("RELATORIO");
            case "OPERADOR":
                return operacao.equalsIgnoreCase("MEDIR") || 
                       operacao.equalsIgnoreCase("VISUALIZAR");
            default:
                return false;
        }
    }

    /**
     * Atualiza o nível de acesso do operador.
     * 
     * @param novoNivel Novo nível de acesso
     * @return boolean true se a atualização foi bem-sucedida
     */
    public boolean atualizarNivelAcesso(String novoNivel) {
        if (novoNivel != null && validarNivelAcesso(novoNivel)) {
            String nivelAnterior = this.nivelAcesso;
            this.nivelAcesso = novoNivel.toUpperCase();
            System.out.println("Nível de acesso do operador " + this.nome + 
                             " atualizado de '" + nivelAnterior + "' para '" + this.nivelAcesso + "'");
            return true;
        }
        return false;
    }

    /**
     * Valida se um email tem formato correto.
     * 
     * @param email Email a ser validado
     * @return boolean true se o email é válido
     */
    private boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    /**
     * Valida se um nível de acesso é válido.
     * 
     * @param nivel Nível a ser validado
     * @return boolean true se o nível é válido
     */
    private boolean validarNivelAcesso(String nivel) {
        if (nivel == null) return false;
        String nivelUpper = nivel.toUpperCase();
        return nivelUpper.equals("ADMIN") || 
               nivelUpper.equals("TECNICO") || 
               nivelUpper.equals("OPERADOR");
    }

    /**
     * Obtém a descrição das permissões baseada no nível de acesso.
     * 
     * @return String com a descrição das permissões
     */
    private String obterDescricaoPermissoes() {
        switch (this.nivelAcesso.toUpperCase()) {
            case "ADMIN":
                return "Acesso total ao sistema";
            case "TECNICO":
                return "Medição, visualização e relatórios";
            case "OPERADOR":
                return "Medição e visualização básica";
            default:
                return "Permissões não definidas";
        }
    }

    /**
     * Verifica se o operador é administrador.
     * 
     * @return boolean true se é administrador
     */
    public boolean isAdmin() {
        return "ADMIN".equals(this.nivelAcesso.toUpperCase());
    }

    /**
     * Retorna uma representação em string do operador.
     * 
     * @return String formatada com as informações principais do operador
     */
    @Override
    public String toString() {
        return String.format("Operator{id='%s', nome='%s', email='%s', nivelAcesso='%s'}", 
                           id, nome, email, nivelAcesso);
    }
}