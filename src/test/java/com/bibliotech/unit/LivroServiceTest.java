package com.bibliotech.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibliotech.model.Livro;
import com.bibliotech.repository.LivroRepository;
import com.bibliotech.service.LivroService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do LivroService")
public class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @InjectMocks
    private LivroService livroService;

    private Livro livroCleanCode;

    @BeforeEach
    void setUp() {
        livroCleanCode = new Livro();
        livroCleanCode.setTitulo("Clean Code");
        livroCleanCode.setAutor("Robert C. Martin");
        livroCleanCode.setIsbn("978-0132350884");
        livroCleanCode.setEditora("Prentice Hall");
        livroCleanCode.setAno(2008);
        livroCleanCode.setQuantidadeExemplares(3);
        livroCleanCode.setQuantidadeDisponivel(3);
    }

    @Test
    @DisplayName("TU-008: Deve lançar exceção ao salvar livro com ISBN duplicado")
    void deveLancarExcecaoISBNJaCadastrado() {
        Livro livroDuplicado = new Livro("Outro Livro", "Outro Autor",
                "978-0132350884", "Editora", 2020, 2);

        when(livroRepository.findByIsbn("978-0132350884"))
                .thenReturn(Optional.of(livroCleanCode));

        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> livroService.salvar(livroDuplicado),
                "Deveria lançar exceção ao tentar salvar ISBN duplicado");

        assertTrue(excecao.getMessage().contains("ISBN já cadastrado"),
                "Mensagem de erro deve informar que ISBN já está cadastrado");
        verify(livroRepository, never()).save(any(Livro.class));
    }

    @Test
    @DisplayName("TU-009: Deve salvar livro com dados válidos e definir quantidade disponível = exemplares")
    void deveSalvarLivroComSucesso() {
        when(livroRepository.findByIsbn(anyString())).thenReturn(Optional.empty());
        when(livroRepository.save(any(Livro.class))).thenReturn(livroCleanCode);

        Livro salvo = livroService.salvar(livroCleanCode);

        assertNotNull(salvo);
        assertEquals(livroCleanCode.getQuantidadeExemplares(), salvo.getQuantidadeDisponivel(),
                "Quantidade disponível deve ser igual à quantidade de exemplares");
        verify(livroRepository).save(livroCleanCode);
    }

    @Test
    @DisplayName("TU-016: Deve lançar exceção ao excluir livro com empréstimos ativos")
    void deveImpedirExclusaoComEmprestimosAtivos() {
        // Simula que o livro tem um empréstimo ativo
        Livro livroComEmprestimo = spy(livroCleanCode);
        when(livroRepository.findById(1L)).thenReturn(Optional.of(livroComEmprestimo));
        // O método getEmprestimos() retorna uma lista que será verificada no service
        // Vamos criar um empréstimo mock que retorna true para isAtivo()
        com.bibliotech.model.Emprestimo emp = mock(com.bibliotech.model.Emprestimo.class);
        when(emp.getAtivo()).thenReturn(true);
        when(livroComEmprestimo.getEmprestimos()).thenReturn(Arrays.asList(emp));

        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> livroService.excluir(1L),
                "Deveria lançar exceção ao excluir livro com empréstimos ativos");

        assertEquals("Não é possível excluir livro com empréstimos ativos", excecao.getMessage());
        verify(livroRepository, never()).delete(any(Livro.class));
    }

    @Test
    @DisplayName("TU-018: Deve retornar apenas livros com quantidade disponível > 0")
    void deveBuscarApenasLivrosDisponiveis() {
        Livro disponivel = new Livro();
        disponivel.setQuantidadeDisponivel(5);
        Livro indisponivel = new Livro();
        indisponivel.setQuantidadeDisponivel(0);

        when(livroRepository.findByQuantidadeDisponivelGreaterThan(0))
                .thenReturn(Arrays.asList(disponivel));

        List<Livro> resultado = livroService.listarDisponiveis();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getQuantidadeDisponivel() > 0);
    }
}