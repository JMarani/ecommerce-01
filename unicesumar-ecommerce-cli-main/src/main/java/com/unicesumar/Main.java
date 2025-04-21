package com.unicesumar;

import com.unicesumar.entities.Product;
import com.unicesumar.entities.Sale;
import com.unicesumar.entities.User;
import com.unicesumar.paymentMethods.PaymentType;
import com.unicesumar.repository.ProductRepository;
import com.unicesumar.repository.SaleRepository;
import com.unicesumar.repository.UserRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        ProductRepository listaDeProdutos = null;
        UserRepository listaDeUsuarios = null;
        SaleRepository saleRepository = null;
        PaymentManager paymentManager = new PaymentManager();

        Connection conn = null;

        // Parâmetros de conexão
        String url = "jdbc:sqlite:database.sqlite";

        // Tentativa de conexão
        try {
            conn = DriverManager.getConnection(url);
            if (conn != null) {
                // Criar as tabelas do banco de dados
                DatabaseSchema schema = new DatabaseSchema(conn);
                schema.createTables();

                // Inicializar os repositórios
                listaDeProdutos = new ProductRepository(conn);
                listaDeUsuarios = new UserRepository(conn);
                saleRepository = new SaleRepository(conn, listaDeProdutos);
            } else {
                System.out.println("Falha na conexão.");
                System.exit(1);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao conectar: " + e.getMessage());
            System.exit(1);
        }

        // Criando o gerenciador de vendas
        SalesManager salesManager = new SalesManager(listaDeUsuarios, listaDeProdutos, saleRepository, paymentManager);

        Scanner scanner = new Scanner(System.in);
        int option;

        do {
            System.out.println("\n---MENU---");
            System.out.println("1 - Cadastrar Produto");
            System.out.println("2 - Listar Produtos");
            System.out.println("3 - Cadastrar Usuário");
            System.out.println("4 - Listar Usuários");
            System.out.println("5 - Registrar Venda");
            System.out.println("6 - Listar Vendas");
            System.out.println("0 - Sair");
            System.out.println("Escolha uma opção: ");
            option = scanner.nextInt();
            scanner.nextLine(); // Consumir a nova linha pendente

            switch (option) {
                case 1:
                    System.out.println("Cadastrar Produto");
                    System.out.print("Nome do produto: ");
                    String nomeProduto = scanner.nextLine();
                    System.out.print("Preço do produto: ");
                    double precoProduto = scanner.nextDouble();
                    scanner.nextLine(); // Consumir a nova linha pendente

                    listaDeProdutos.save(new Product(nomeProduto, precoProduto));
                    System.out.println("Produto cadastrado com sucesso!");
                    break;
                case 2:
                    System.out.println("Listar Produtos");
                    List<Product> products = listaDeProdutos.findAll();
                    if (products.isEmpty()) {
                        System.out.println("Nenhum produto cadastrado!");
                    } else {
                        System.out.println("ID | Nome | Preço");
                        for (Product p : products) {
                            System.out.println(p.getUuid() + " | " + p.getName() + " | R$ " + p.getPrice());
                        }
                    }
                    break;
                case 3:
                    System.out.println("Cadastrar Usuário");
                    System.out.print("Nome do usuário: ");
                    String nomeUsuario = scanner.nextLine();
                    System.out.print("Email do usuário: ");
                    String emailUsuario = scanner.nextLine();
                    System.out.print("Senha do usuário: ");
                    String senhaUsuario = scanner.nextLine();

                    listaDeUsuarios.save(new User(nomeUsuario, emailUsuario, senhaUsuario));
                    System.out.println("Usuário cadastrado com sucesso!");
                    break;
                case 4:
                    System.out.println("Listar Usuários");
                    List<User> users = listaDeUsuarios.findAll();
                    if (users.isEmpty()) {
                        System.out.println("Nenhum usuário cadastrado!");
                    } else {
                        System.out.println("ID | Nome | Email");
                        for (User u : users) {
                            System.out.println(u.getUuid() + " | " + u.getName() + " | " + u.getEmail());
                        }
                    }
                    break;
                case 5:
                    registrarVenda(scanner, salesManager);
                    break;
                case 6:
                    listarVendas(saleRepository, listaDeUsuarios);
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente");
                    break;
            }

        } while (option != 0);

        scanner.close();
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void registrarVenda(Scanner scanner, SalesManager salesManager) {
        System.out.println("\n--- REGISTRO DE VENDA ---");

        // 1. Buscar usuário por email
        System.out.print("Digite o Email do usuário: ");
        String email = scanner.nextLine();

        Optional<User> usuarioOpt = salesManager.findUserByEmail(email);
        if (usuarioOpt.isEmpty()) {
            System.out.println("Usuário não encontrado!");
            return;
        }

        User usuario = usuarioOpt.get();
        System.out.println("Usuário encontrado: " + usuario.getName());

        // 2. Buscar produtos por IDs
        System.out.print("Digite o ID do produto: ");
        String idsInput = scanner.nextLine();

        List<UUID> produtosIds = new ArrayList<>();
        try {
            for (String id : idsInput.split(",")) {
                produtosIds.add(UUID.fromString(id.trim()));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Formato de ID inválido!");
            return;
        }

        List<Product> produtos = salesManager.findProductsByIds(produtosIds);
        if (produtos.isEmpty()) {
            System.out.println("Nenhum produto encontrado com os IDs informados!");
            return;
        }

        System.out.println("Produtos encontrados:");
        for (Product produto : produtos) {
            System.out.println("- " + produto.getName() + " (R$ " + produto.getPrice() + ")");
        }

        double valorTotal = salesManager.calculateTotal(produtos);

        // 3. Escolher forma de pagamento
        System.out.println("\nEscolha a forma de pagamento:");
        System.out.println("1 - Cartão de Crédito");
        System.out.println("2 - Boleto");
        System.out.println("3 - PIX");
        System.out.print("Opção: ");
        int opcaoPagamento = scanner.nextInt();
        scanner.nextLine(); // Consumir a nova linha pendente

        PaymentType paymentType;
        switch (opcaoPagamento) {
            case 1:
                paymentType = PaymentType.CARTAO;
                break;
            case 2:
                paymentType = PaymentType.BOLETO;
                break;
            case 3:
                paymentType = PaymentType.PIX;
                break;
            default:
                System.out.println("Opção de pagamento inválida!");
                return;
        }

        System.out.println("Aguarde, efetuando pagamento...");

        // 4. Criar e salvar a venda
        Sale venda = salesManager.createSale(usuario, produtos, paymentType);

        // 5. Mostrar resumo da venda
        System.out.println("\nResumo da venda:");
        System.out.println("Cliente: " + usuario.getName());
        System.out.println("Produtos:");
        for (Product produto : produtos) {
            System.out.println("- " + produto.getName());
        }
        System.out.println("Valor total: R$ " + valorTotal);
        System.out.println("Pagamento: " + paymentType);

        System.out.println("\nVenda registrada com sucesso!");
    }

    private static void listarVendas(SaleRepository saleRepository, UserRepository userRepository) {
        System.out.println("\n--- LISTA DE VENDAS ---");

        List<Sale> vendas = saleRepository.findAll();

        if (vendas.isEmpty()) {
            System.out.println("Nenhuma venda registrada!");
            return;
        }

        for (Sale venda : vendas) {
            System.out.println("ID: " + venda.getUuid());

            Optional<User> user = userRepository.findById(venda.getUserId());
            System.out.println("Cliente: " + (user.isPresent() ? user.get().getName() : "Usuário não encontrado"));

            System.out.println("Data: " + venda.getSaleDate());
            System.out.println("Forma de pagamento: " + venda.getPaymentMethod());

            System.out.println("Produtos:");
            for (Product produto : venda.getProducts()) {
                System.out.println("- " + produto.getName() + " (R$ " + produto.getPrice() + ")");
            }

            System.out.println("Valor total: R$ " + venda.getTotal());
            System.out.println("---");
        }
    }
}
