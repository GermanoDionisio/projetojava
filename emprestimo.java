import java.util.*;
import java.io.*;

public class Biblioteca {
    private List<Livro> livros = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private List<Emprestimo> emprestimos = new ArrayList<>();
    private Map<Livro, Integer> rankingEmprestimos = new HashMap<>();
    private Map<Usuario, Double> penalidadesPendentes = new HashMap<>();

    // CRUD Livro
    public void adicionarLivro(Livro livro) {
        livros.add(livro);
        rankingEmprestimos.put(livro, 0);
    }
    public boolean removerLivro(String isbn) {
        Livro l = buscarLivroPorIsbn(isbn);
        if (l != null) {
            livros.remove(l);
            rankingEmprestimos.remove(l);
            return true;
        }
        return false;
    }
    public Livro buscarLivroPorIsbn(String isbn) {
        for (Livro l : livros)
            if (l.getIsbn().equals(isbn)) return l;
        return null;
    }
    public List<Livro> buscarLivrosPorTitulo(String titulo) {
        List<Livro> result = new ArrayList<>();
        for (Livro l : livros)
            if (l.getTitulo().toLowerCase().contains(titulo.toLowerCase()))
                result.add(l);
        return result;
    }
    public List<Livro> buscarLivrosPorAutor(String autor) {
        List<Livro> result = new ArrayList<>();
        for (Livro l : livros)
            if (l.getAutor().toLowerCase().contains(autor.toLowerCase()))
                result.add(l);
        return result;
    }

    // CRUD Usuario
    public void adicionarUsuario(Usuario usuario) {
        usuarios.add(usuario);
        penalidadesPendentes.put(usuario, 0.0);
    }
    public boolean removerUsuario(String id) {
        Usuario u = buscarUsuarioPorId(id);
        if (u != null) {
            usuarios.remove(u);
            penalidadesPendentes.remove(u);
            return true;
        }
        return false;
    }
    public Usuario buscarUsuarioPorId(String id) {
        for (Usuario u : usuarios)
            if (u.getId().equals(id)) return u;
        return null;
    }
    public List<Usuario> buscarUsuarioPorNome(String nome) {
        List<Usuario> result = new ArrayList<>();
        for (Usuario u : usuarios)
            if (u.getNome().toLowerCase().contains(nome.toLowerCase()))
                result.add(u);
        return result;
    }

    // Ranking de livros
    public List<Livro> rankingLivrosMaisEmprestados(int topN) {
        List<Livro> ordem = new ArrayList<>(livros);
        ordem.sort((l1, l2) -> rankingEmprestimos.getOrDefault(l2,0) - rankingEmprestimos.getOrDefault(l1,0));
        return ordem.subList(0, Math.min(topN, ordem.size()));
    }

    // Operação empréstimo
    public Emprestimo emprestarLivro(String usuarioId, String isbn) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        Livro livro = buscarLivroPorIsbn(isbn);
        if (usuario == null || livro == null || !livro.isDisponivel())
            return null;
        if (getPenalidade(usuario) > 0)
            return null;

        Emprestimo e = new Emprestimo(usuario, livro);
        emprestimos.add(e);
        rankingEmprestimos.put(livro, rankingEmprestimos.getOrDefault(livro,0)+1);
        return e;
    }

    // Devolução com penalização automática
    public boolean devolverLivro(String usuarioId, String isbn) {
        for (Emprestimo e : emprestimos)
            if (e.emAberto() && e.usuario.getId().equals(usuarioId) && e.livro.getIsbn().equals(isbn)) {
                e.devolver();
                long dias = (e.getDataDevolucao().getTime() - e.getDataEmprestimo().getTime())/(1000*60*60*24);
                if (dias > 7) {
                    double multa = (dias-7)*2.5;
                    penalidadesPendentes.put(e.usuario, penalidadesPendentes.getOrDefault(e.usuario,0.0)+multa);
                }
                return true;
            }
        return false;
    }

    public List<Emprestimo> listarEmprestimosUsuario(String usuarioId) {
        List<Emprestimo> list = new ArrayList<>();
        for (Emprestimo e : emprestimos)
            if (e.usuario.getId().equals(usuarioId))
                list.add(e);
        return list;
    }
    public List<Emprestimo> listarEmprestimosAbertos() {
        List<Emprestimo> list = new ArrayList<>();
        for (Emprestimo e : emprestimos)
            if (e.emAberto())
                list.add(e);
        return list;
    }
    public List<Livro> listarLivrosDisponiveis() {
        List<Livro> list = new ArrayList<>();
        for (Livro l : livros)
            if (l.isDisponivel())
                list.add(l);
        return list;
    }

    // Penalidade
    public double getPenalidade(Usuario usuario) {
        return penalidadesPendentes.getOrDefault(usuario, 0.0);
    }
    public boolean pagarPenalidade(String usuarioId, double valor) {
        Usuario usuario = buscarUsuarioPorId(usuarioId);
        if (usuario != null) {
            double pendente = getPenalidade(usuario);
            if (valor >= pendente) {
                penalidadesPendentes.put(usuario, 0.0);
                return true;
            }
        }
        return false;
    }

    // Exportação de relatório para arquivo TXT
    public void exportarRelatorioEmprestimos(String filename) {
        try (PrintWriter writer = new PrintWriter(new File(filename))) {
            for (Emprestimo e : emprestimos)
                writer.println(e.toString());
        } catch (IOException ex) {
            System.out.println("Erro ao exportar: " + ex.getMessage());
        }
    }

    // Relatório sintético de desempenho do sistema
    public void mostrarRelatorioCompleto() {
        System.out.println("--- LIVROS MAIS EMPRESTADOS ---");
        for (Livro l : rankingLivrosMaisEmprestados(Math.min(5, livros.size())))
            System.out.println(l.getTitulo() + ": " + rankingEmprestimos.getOrDefault(l, 0));
        System.out.println("--- USUÁRIOS COM PENALIDADES ---");
        for (Usuario u : usuarios) {
            double pendente = getPenalidade(u);
            if (pendente > 0)
                System.out.println(u.getNome() + ", pendente: R$" + String.format("%.2f", pendente));
        }
        System.out.println("--- TOTAL DE USUÁRIOS: " + usuarios.size());
        System.out.println("--- TOTAL DE LIVROS: " + livros.size());
    }

    // Utilitário: listar livros nunca emprestados
    public List<Livro> listarLivrosNuncaEmprestados() {
        List<Livro> nunca = new ArrayList<>();
        for (Livro l : livros)
            if (rankingEmprestimos.getOrDefault(l, 0) == 0)
                nunca.add(l);
        return nunca;
    }

    // Utilitário: listar usuários sem penalidades
    public List<Usuario> listarUsuariosSemPenalidades() {
        List<Usuario> lista = new ArrayList<>();
        for (Usuario u : usuarios)
            if (getPenalidade(u) == 0)
                lista.add(u);
        return lista;
    }

    // Utilitário extra: buscar empréstimos em atraso (> 7 dias)
    public List<Emprestimo> listarEmprestimosEmAtraso() {
        List<Emprestimo> atrasados = new ArrayList<>();
        Date agora = new Date();
        for (Emprestimo e : listarEmprestimosAbertos()) {
            long dias = (agora.getTime() - e.getDataEmprestimo().getTime())/(1000*60*60*24);
            if (dias > 7) atrasados.add(e);
        }
        return atrasados;
    }
}
