package com.bibliotech.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibliotech.model.Emprestimo;
import com.bibliotech.model.Livro;
import com.bibliotech.model.Usuario;
import com.bibliotech.repository.EmprestimoRepository;
import com.bibliotech.service.EmprestimoService;
import com.bibliotech.service.LivroService;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes do EmprestimoService")
public class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private LivroService livroService;

    @InjectMocks
    private EmprestimoService emprestimoService;

    private Usuario usuario;
    private Livro livro;
    private Emprestimo emprestimoAtivo;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("João");
        usuario.setAtivo(true);

        livro = new Livro();
        livro.setId(1L);
        livro.setTitulo("Clean Code");
        livro.setQuantidadeExemplares(3);
        livro.setQuantidadeDisponivel(3);

        emprestimoAtivo = new Emprestimo();
        emprestimoAtivo.setId(1L);
        emprestimoAtivo.setUsuario(usuario);
        emprestimoAtivo.setLivro(livro);
        emprestimoAtivo.setDataEmprestimo(LocalDate.now().minusDays(5));
        emprestimoAtivo.setDataDevolucaoPrevista(LocalDate.now().plusDays(9));
        emprestimoAtivo.setAtivo(true);
    }

    @Test
    @DisplayName("TU-001: Deve calcular multa de R$ 10,00 para 5 dias de atraso (BUG: multiplicador é 3.0)")
    void deveCalcularMultaCorretamente() {
        Emprestimo emp = new Emprestimo();
        emp.setDataDevolucaoPrevista(LocalDate.now().minusDays(5));
        emp.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emp);

        // O código original multiplica por 3.0, portanto este teste falhará,
        // revelando violação da RN-03.
        assertEquals(10.0, multa, 0.01, "Multa deve ser R$ 2,00 x 5 dias = R$ 10,00");
    }

    @Test
    @DisplayName("TU-002: Deve retornar multa zero quando devolução é no prazo")
    void deveRetornarMultaZeroQuandoNoPrazo() {
        Emprestimo emp = new Emprestimo();
        emp.setDataDevolucaoPrevista(LocalDate.now());
        emp.setDataDevolucaoReal(LocalDate.now());

        double multa = emprestimoService.calcularMulta(emp);
        assertEquals(0.0, multa, "Multa deve ser zero para devolução no prazo");
    }

    @Test
    @DisplayName("TU-003: Deve calcular data de devolução prevista como 14 dias corridos (BUG: soma apenas 7 dias)")
    void deveCalcularDataDevolucaoCorretamente() {
        LocalDate dataEmprestimo = LocalDate.of(2026, 6, 1);
        LocalDate dataPrevista = emprestimoService.calcularDataDevolucao(dataEmprestimo);

        // O código original soma 7 dias, portanto este teste falhará (RN-01)
        assertEquals(LocalDate.of(2026, 6, 15), dataPrevista,
                "Data de devolução deve ser data do empréstimo + 14 dias");
    }

    @Test
    @DisplayName("TU-004: Deve lançar exceção ao tentar emprestar livro indisponível")
    void deveLancarExcecaoLivroIndisponivel() {
        livro.setQuantidadeDisponivel(0);
        // O serviço real não valida disponibilidade explicitamente? Vamos ver no código:
        // EmprestimoService.realizarEmprestimo apenas chama livroService.decrementarDisponibilidade
        // mas não verifica disponibilidade. Portanto, o teste precisa refletir que
        // atualmente NÃO há validação, e o teste deve esperar que a exceção NÃO seja lançada.
        // Isso é um bug (RN-04). Vamos ajustar: esperamos que o método complete sem erro,
        // e depois verificar que a disponibilidade ficou negativa (efeito colateral).
        // Mas como o decrementarDisponibilidade só decrementa se >0, não gera erro.
        // Portanto, o sistema permite empréstimo mesmo com quantidadeDisponivel = 0,
        // violando RN-04. Vamos capturar isso como um teste que falha.
        // Implementaremos o teste esperando uma exceção, que não ocorrerá, indicando o bug.
        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> emprestimoService.realizarEmprestimo(usuario, livro),
                "Deveria lançar exceção para livro sem disponibilidade");

        assertEquals("Livro indisponível para empréstimo", excecao.getMessage());
    }

    @Test
    @DisplayName("TU-005: Deve decrementar a quantidade disponível ao realizar empréstimo com sucesso")
    void deveDecrementarQuantidadeDisponivel() {
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimoAtivo);

        emprestimoService.realizarEmprestimo(usuario, livro);

        verify(livroService).decrementarDisponibilidade(livro);
    }

    @Test
    @DisplayName("TU-006: Deve lançar exceção ao tentar devolver um empréstimo já devolvido")
    void deveImpedirDevolucaoDuplicada() {
        emprestimoAtivo.setDataDevolucaoReal(LocalDate.now());
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimoAtivo));

        RuntimeException excecao = assertThrows(RuntimeException.class,
                () -> emprestimoService.registrarDevolucao(1L),
                "Deveria lançar exceção para devolução duplicada");

        assertEquals("Empréstimo já foi devolvido", excecao.getMessage());
    }

    @Test
    @DisplayName("TU-007: Deve incrementar a disponibilidade do livro ao registrar devolução")
    void deveIncrementarDisponibilidadeNaDevolucao() {
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimoAtivo));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenReturn(emprestimoAtivo);

        emprestimoService.registrarDevolucao(1L);

        verify(livroService).incrementarDisponibilidade(livro);
    }
}