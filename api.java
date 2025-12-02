import java.util.*;
import java.util.stream.Collectors;

/**
 * Sistema completo simulado de API RESTful para E-commerce.
 * Contém todos os elementos necessários para cadastrar usuários e produtos,
 * autenticar usuários, criar e pagar pedidos, com controle de estoque,
 * tudo em uma aplicação de linha de comando organizada e comentada.
 */
public class ECommerceApp {

    /**
     * Classe que representa um produto no catálogo.
     * Possui atributos de id, nome, preço e estoque, com métodos para manipular estoque.
     */
    public static class Produto {
        private int id;
        private String nome;
        private double preco;
        private int estoque;

        public Produto(int id, String nome, double preco, int estoque) {
            this.id = id;
            this.nome = nome;
            this.preco = preco;
            this.estoque = estoque;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public double getPreco() { return preco; }
        public int getEstoque() { return estoque; }

        /**
         * Adiciona unidades ao estoque do produto.
         * @param quantidade quantidade a adicionar
         */
        public void adicionarEstoque(int quantidade) {
            if (quantidade > 0) this.estoque += quantidade;
        }

        /**
         * Reduz o estoque do produto se houver quantidade suficiente.
         * @param quantidade quantidade a reduzir
         * @return true se operação for bem sucedida
         */
        public boolean reduzirEstoque(int quantidade) {
            if (quantidade > 0 && estoque >= quantidade) {
                estoque -= quantidade;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "Produto[id=" + id + ", nome='" + nome + "', preço=R$" + String.format("%.2f", preco) + ", estoque=" + estoque + "]";
        }
    }

    /**
     * Classe que representa um usuário do sistema.
     * Contém dados basicos, senha (em texto simples para exemplo) e flag de administrador.
     */
    public static class Usuario {
        private int id;
        private String nome;
        private String email;
        private String senha;
        private boolean admin;

        public Usuario(int id, String nome, String email, String senha, boolean admin) {
            this.id = id;
            this.nome = nome;
            this.email = email;
            this.senha = senha;
            this.admin = admin;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }

        /**
         * Verifica se a senha fornecida bate com a senha do usuário.
         * @param senha senha para autenticar
         * @return true se senha estiver correta
         */
        public boolean autenticar(String senha) {
            return this.senha.equals(senha);
        }

        public boolean isAdmin() {
            return admin;
        }

        @Override
        public String toString() {
            return "Usuário[id=" + id + ", nome='" + nome + "', email='" + email + "', admin=" + admin + "]";
        }
    }

    /**
     * Classe que representa um pedido feito por um usuário.
     * Contém múltiplos itens e quantidades, calcula total e controla pagamento e reduções de estoque.
     */
    public static class Pedido {
        private int id;
        private Usuario comprador;
        private Date dataPedido;
        private Map<Produto, Integer> itens = new HashMap<>();
        private boolean pago;

        public Pedido(int id, Usuario comprador) {
            this.id = id;
            this.comprador = comprador;
            this.dataPedido = new Date();
            this.pago = false;
        }

        public int getId() { return id; }
        public Usuario getComprador() { return comprador; }
        public Date getDataPedido() { return dataPedido; }
        public boolean isPago() { return pago; }

        /**
         * Tenta adicionar item ao pedido.
         * Verifica validade da quantidade e estoque disponível antes de adicionar.
         * @param produto produto a ser adicionado
         * @param quantidade quantidade desejada
         * @return true se adicionado com sucesso
         */
        public boolean adicionarItem(Produto produto, int quantidade) {
            if (produto == null || quantidade <= 0 || produto.getEstoque() < quantidade) return false;

            if (itens.containsKey(produto)) {
                int atual = itens.get(produto);
                if (produto.getEstoque() < atual + quantidade) return false;
                itens.put(produto, atual + quantidade);
            } else {
                itens.put(produto, quantidade);
            }
            return true;
        }

        /**
         * Remove item do pedido.
         * @param produto produto a remover
         * @return true se removido
         */
        public boolean removerItem(Produto produto) {
            if (produto == null || !itens.containsKey(produto)) return false;
            itens.remove(produto);
            return true;
        }

        /**
         * Marca pedido como pago e efetua a redução de estoque dos produtos.
         * Verifica se há estoque suficiente antes da aprovação.
         * @return true se o pagamento e atualização forem efetuados
         */
        public boolean pagarPedido() {
            if (pago) return false; // Já pago
            for (Map.Entry<Produto, Integer> entry : itens.entrySet()) {
                if (entry.getKey().getEstoque() < entry.getValue()) return false; // Estoque insuficiente
            }
            for (Map.Entry<Produto, Integer> entry : itens.entrySet()) {
                entry.getKey().reduzirEstoque(entry.getValue());
            }
            pago = true;
            return true;
        }

        /**
         * Calcula o valor total do pedido baseado em todos os itens e quantidades.
         * @return valor total do pedido
         */
        public double total() {
            double soma = 0;
            for (Map.Entry<Produto, Integer> entry : itens.entrySet()) {
                soma += entry.getKey().getPreco() * entry.getValue();
            }
            return soma;
        }

        public Map<Produto, Integer> getItens() {
            return Collections.unmodifiableMap(itens);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Pedido #").append(id).append(" - Comprador: ").append(comprador.getNome()).append("\n");
            sb.append("Data: ").append(dataPedido).append(", Pago: ").append(pago).append("\nItens:\n");
            for (Map.Entry<Produto, Integer> item : itens.entrySet()) {
                sb.append(" - ").append(item.getKey().getNome())
                  .append(" x").append(item.getValue())
                  .append(" = R$").append(String.format("%.2f", item.getKey().getPreco() * item.getValue()))
                  .append("\n");
            }
            sb.append("Total: R$").append(String.format("%.2f", total())).append("\n");
            return sb.toString();
        }
    }

    /**
     * Controladora simples de autenticação.
     * Mapeia usuários por email para facilitar login.
     */
    public static class AuthController {
        private Map<String, Usuario> usuariosPorEmail = new HashMap<>();

        /**
         * Inicializa com lista de usuários.
         * @param usuarios lista de usuários registrados
         */
        public AuthController(List<Usuario> usuarios) {
            for (Usuario u : usuarios) {
                usuariosPorEmail.put(u.getEmail(), u);
            }
        }

        /**
         * Autentica usuário com email e senha.
         * @param email email fornecido
         * @param senha senha fornecida
         * @return usuário autenticado ou null se falhou
         */
        public Usuario login(String email, String senha) {
            Usuario usu = usuariosPorEmail.get(email);
            if (usu != null && usu.autenticar(senha)) {
                return usu;
            }
            return null;
        }
    }

    /**
     * Método principal que roda toda a aplicação.
     * Simula interações típicas de um usuário com sistema de e-commerce via linha de comando.
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Listas para armazenar dados em memória
        List<Usuario> usuarios = new ArrayList<>();
        List<Produto> produtos = new ArrayList<>();
        List<Pedido> pedidos = new ArrayList<>();

        // Inicialização com dados de exemplo
        usuarios.add(new Usuario(1, "Admin", "admin@shop.com", "admin123", true));
        usuarios.add(new Usuario(2, "João Cliente", "joao@mail.com", "passjoao", false));

        produtos.add(new Produto(1, "Smartphone", 1800.50, 10));
        produtos.add(new Produto(2, "Fone Bluetooth", 350.99, 25));
        produtos.add(new Produto(3, "Mouse Gamer", 150.00, 40));

        AuthController authController = new AuthController(usuarios);

        // Login
        System.out.println("Bem-vindo à API RESTful Simulada E-commerce");
        System.out.print("Informe seu email: ");
        String email = sc.nextLine();
        System.out.print("Informe sua senha: ");
        String senha = sc.nextLine();

        Usuario usuario = authController.login(email, senha);

        if (usuario == null) {
            System.out.println("Falha na autenticação. Encerrando.");
            sc.close();
            return;
        }

        System.out.println("Login realizado com sucesso! Usuário: " + usuario.getNome());

        // Criação do pedido inicial
        Pedido pedido = new Pedido(pedidos.size() + 1, usuario);

        boolean continuar = true;

        while (continuar) {
            System.out.println("\nMenu:");
            System.out.println("1 - Listar produtos");
            System.out.println("2 - Adicionar produto ao pedido");
            System.out.println("3 - Finalizar pagamento do pedido");
            System.out.println("4 - Mostrar resumo do pedido");
            System.out.println("0 - Sair");

            int opcao = -1;
            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (Exception ignored) {}

            switch (opcao) {
                case 1:
                    System.out.println("Produtos disponíveis:");
                    for (Produto p : produtos) {
                        System.out.println(p);
                    }
                    break;
                case 2:
                    System.out.print("Informe o ID do produto: ");
                    int produtoId = Integer.parseInt(sc.nextLine());
                    Produto produtoSelecionado = null;
                    for (Produto p : produtos) {
                        if (p.getId() == produtoId) {
                            produtoSelecionado = p;
                            break;
                        }
                    }
                    if (produtoSelecionado == null) {
                        System.out.println("Produto não encontrado.");
                        break;
                    }
                    System.out.print("Quantidade: ");
                    int qtd = Integer.parseInt(sc.nextLine());
                    if (pedido.adicionarItem(produtoSelecionado, qtd)) {
                        System.out.println("Produto adicionado ao pedido com sucesso.");
                    } else {
                        System.out.println("Erro: Quantidade inválida ou estoque insuficiente.");
                    }
                    break;
                case 3:
                    if (pedido.getItens().isEmpty()) {
                        System.out.println("Pedido vazio! Adicione produtos antes de pagar.");
                        break;
                    }
                    if (pedido.pagarPedido()) {
                        pedidos.add(pedido);
                        System.out.println("Pedido pago com sucesso! Detalhes:\n" + pedido);
                        continuar = false; // Encerra após pagamento
                    } else {
                        System.out.println("Erro ao pagar. Verifique estoque dos produtos.");
                    }
                    break;
                case 4:
                    System.out.println("Resumo do pedido atual:");
                    System.out.println(pedido);
                    break;
                case 0:
                    continuar = false;
                    System.out.println("Saindo do sistema. Obrigado!");
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
        sc.close();
    }
}
