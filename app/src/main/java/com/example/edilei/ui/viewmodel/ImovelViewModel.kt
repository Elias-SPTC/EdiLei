package com.example.edilei.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edilei.data.model.CategoriaCusto
import com.example.edilei.data.model.Custo
import com.example.edilei.data.model.Imovel
import com.example.edilei.data.model.ResumoFinanceiro
import com.example.edilei.data.model.Socio
import com.example.edilei.data.model.StatusImovel
import com.example.edilei.data.repository.ImovelRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ImovelViewModel(
    private val repository: ImovelRepository = ImovelRepository()
) : ViewModel() {

    val socios: StateFlow<List<Socio>> = repository.socios
    val imoveis: StateFlow<List<Imovel>> = repository.imoveis

    private val _selectedImovelId = MutableStateFlow<String?>(null)
    val selectedImovelId: StateFlow<String?> = _selectedImovelId.asStateFlow()

    private val _currentSocioId = MutableStateFlow<String>("socio_1")
    val currentSocioId: StateFlow<String> = _currentSocioId.asStateFlow()

    val custosDoImovelSelecionado: StateFlow<List<Custo>> = _selectedImovelId.flatMapLatest { id ->
        if (id != null) {
            repository.getCustosPorImovel(id)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resumoFinanceiro: StateFlow<ResumoFinanceiro?> = _selectedImovelId.flatMapLatest { id ->
        if (id != null) {
            repository.calcularResumoFinanceiro(id)
        } else {
            flowOf(null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun selecionarImovel(id: String?) {
        _selectedImovelId.value = id
    }

    fun selecionarSocioAtual(socioId: String) {
        _currentSocioId.value = socioId
    }

    fun salvarSocio(id: String?, nome: String, email: String, corHex: String) {
        val socioId = id ?: UUID.randomUUID().toString()
        val socio = Socio(id = socioId, nome = nome, email = email, corHex = corHex)
        repository.salvarSocio(socio)
        if (_currentSocioId.value.isBlank() || id == null) {
            _currentSocioId.value = socioId
        }
    }

    fun efetivarCusto(
        custoId: String,
        valorEfetivado: Double,
        socioId: String,
        observacoes: String
    ) {
        repository.efetivarCusto(custoId, valorEfetivado, socioId, observacoes)
    }

    fun adicionarImovel(
        titulo: String,
        rua: String,
        numero: String,
        bairro: String,
        cidade: String,
        uf: String,
        condominio: String,
        bloco: String,
        apartamento: String,
        bancoOrigem: String,
        valorAvaliacao: Double,
        valorMatricula: Double,
        valorLanceArrematado: Double,
        valorVendaPretendido: Double
    ) {
        val newId = UUID.randomUUID().toString()
        val novoImovel = Imovel(
            id = newId,
            titulo = titulo,
            rua = rua,
            numero = numero,
            bairro = bairro,
            cidade = cidade,
            uf = uf,
            condominio = condominio,
            bloco = bloco,
            apartamento = apartamento,
            bancoOrigem = bancoOrigem,
            status = StatusImovel.ARREMATADO,
            valorAvaliacao = valorAvaliacao,
            valorMatricula = valorMatricula,
            valorLanceArrematado = valorLanceArrematado,
            valorVendaPretendido = valorVendaPretendido
        )

        val comissaoLeiloeiro = valorLanceArrematado * 0.05
        val itbiValor = valorLanceArrematado * 0.03
        val comissaoVenda = valorVendaPretendido * 0.06

        val custosPadrao = listOf(
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Valor do Lance Arrematado",
                categoria = CategoriaCusto.ARREMATACAO,
                valorEstimado = valorLanceArrematado
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Comissão do Leiloeiro (5%)",
                categoria = CategoriaCusto.ARREMATACAO,
                valorEstimado = comissaoLeiloeiro
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "IPTU Atrasado",
                categoria = CategoriaCusto.DEBITOS_ANTERIORES,
                valorEstimado = 4500.0
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Condomínio Atrasado",
                categoria = CategoriaCusto.DEBITOS_ANTERIORES,
                valorEstimado = 3500.0
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Honorários advocatícios",
                categoria = CategoriaCusto.JURIDICO_DESOCUPACAO,
                valorEstimado = 4500.0
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "ITBI",
                categoria = CategoriaCusto.CARTORIO_IMPOSTOS,
                valorEstimado = itbiValor
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Escritura e Registro",
                categoria = CategoriaCusto.CARTORIO_IMPOSTOS,
                valorEstimado = 3500.0
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Pintura e Reparos",
                categoria = CategoriaCusto.MANUTENCAO_REFORMA,
                valorEstimado = 8500.0
            ),
            Custo(
                id = UUID.randomUUID().toString(),
                imovelId = newId,
                titulo = "Comissão da Venda (6%)",
                categoria = CategoriaCusto.VENDA_DESINVESTIMENTO,
                valorEstimado = comissaoVenda
            )
        )

        repository.adicionarImovel(novoImovel, custosPadrao)
        _selectedImovelId.value = newId
    }

    fun editarImovel(imovel: Imovel) {
        repository.editarImovel(imovel)
    }

    fun removerImovel(imovelId: String) {
        repository.removerImovel(imovelId)
        if (_selectedImovelId.value == imovelId) {
            _selectedImovelId.value = null
        }
    }

    fun adicionarCusto(
        titulo: String,
        categoria: CategoriaCusto,
        valorEstimado: Double
    ) {
        val imovelId = _selectedImovelId.value ?: return
        val novoCusto = Custo(
            id = UUID.randomUUID().toString(),
            imovelId = imovelId,
            titulo = titulo,
            categoria = categoria,
            valorEstimado = valorEstimado
        )
        repository.adicionarCusto(novoCusto)
    }

    fun editarCusto(custoId: String, novoTitulo: String, novoValorEstimado: Double) {
        repository.editarCusto(custoId, novoTitulo, novoValorEstimado)
    }

    fun removerCusto(custoId: String) {
        repository.removerCusto(custoId)
    }
}
