package com.example.edilei.data.repository

import com.example.edilei.data.model.AporteSocio
import com.example.edilei.data.model.CategoriaCusto
import com.example.edilei.data.model.Custo
import com.example.edilei.data.model.Imovel
import com.example.edilei.data.model.ResumoFinanceiro
import com.example.edilei.data.model.Socio
import com.example.edilei.data.model.StatusCusto
import com.example.edilei.data.model.StatusImovel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ImovelRepository {

    private val firestore: FirebaseFirestore? by lazy {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val _socios = MutableStateFlow(
        listOf(
            Socio("socio_1", "Sócio A (Elias)", "elias@empresa.com", "#1E88E5"),
            Socio("socio_2", "Sócio B (Carlos)", "carlos@empresa.com", "#43A047")
        )
    )
    val socios: StateFlow<List<Socio>> = _socios.asStateFlow()

    private val _imoveis = MutableStateFlow<List<Imovel>>(emptyList())
    val imoveis: StateFlow<List<Imovel>> = _imoveis.asStateFlow()

    private val _custos = MutableStateFlow<List<Custo>>(emptyList())
    val custos: StateFlow<List<Custo>> = _custos.asStateFlow()

    private var sociosListener: ListenerRegistration? = null
    private var imoveisListener: ListenerRegistration? = null
    private val custosListeners = mutableMapOf<String, ListenerRegistration>()

    init {
        carregarDadosIniciaisLocal()
        iniciarSincronizacaoFirestore()
    }

    private fun iniciarSincronizacaoFirestore() {
        val db = firestore ?: return

        // Observa coleção de sócios
        runCatching {
            sociosListener = db.collection("socios").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val listaSociosNuvem = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        Socio(
                            id = doc.id,
                            nome = doc.getString("nome") ?: "",
                            email = doc.getString("email") ?: "",
                            corHex = doc.getString("corHex") ?: "#1E88E5"
                        )
                    }.getOrNull()
                }
                if (listaSociosNuvem.isNotEmpty()) {
                    _socios.value = listaSociosNuvem
                }
            }
        }

        // Observa coleção de imóveis na nuvem em tempo real
        runCatching {
            imoveisListener = db.collection("imoveis").addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val listaNuvem = snapshot.documents.mapNotNull { doc ->
                    runCatching {
                        Imovel(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            endereco = doc.getString("endereco") ?: "",
                            rua = doc.getString("rua") ?: "",
                            numero = doc.getString("numero") ?: "",
                            bairro = doc.getString("bairro") ?: "",
                            cidade = doc.getString("cidade") ?: "",
                            uf = doc.getString("uf") ?: "MG",
                            condominio = doc.getString("condominio") ?: "",
                            bloco = doc.getString("bloco") ?: "",
                            apartamento = doc.getString("apartamento") ?: "",
                            bancoOrigem = doc.getString("bancoOrigem") ?: "CAIXA",
                            status = runCatching {
                                StatusImovel.valueOf(doc.getString("status") ?: "ARREMATADO")
                            }.getOrDefault(StatusImovel.ARREMATADO),
                            valorAvaliacao = doc.getDouble("valorAvaliacao") ?: 0.0,
                            valorMatricula = doc.getDouble("valorMatricula") ?: 0.0,
                            valorLanceArrematado = doc.getDouble("valorLanceArrematado") ?: 0.0,
                            valorVendaPretendido = doc.getDouble("valorVendaPretendido") ?: 0.0,
                            valorVendaRealizado = doc.getDouble("valorVendaRealizado"),
                            criadoEm = doc.getLong("criadoEm") ?: System.currentTimeMillis()
                        )
                    }.getOrNull()
                }

                if (listaNuvem.isNotEmpty()) {
                    _imoveis.value = listaNuvem
                    listaNuvem.forEach { imovel -> observarCustosDoImovelNuvem(imovel.id) }
                }
            }
        }
    }

    private fun observarCustosDoImovelNuvem(imovelId: String) {
        val db = firestore ?: return
        if (custosListeners.containsKey(imovelId)) return

        runCatching {
            val listener = db.collection("imoveis").document(imovelId).collection("custos")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val custosNuvem = snapshot.documents.mapNotNull { doc ->
                        runCatching {
                            Custo(
                                id = doc.id,
                                imovelId = doc.getString("imovelId") ?: imovelId,
                                titulo = doc.getString("titulo") ?: "",
                                categoria = runCatching {
                                    CategoriaCusto.valueOf(doc.getString("categoria") ?: "MANUTENCAO_REFORMA")
                                }.getOrDefault(CategoriaCusto.MANUTENCAO_REFORMA),
                                valorEstimado = doc.getDouble("valorEstimado") ?: 0.0,
                                valorEfetivado = doc.getDouble("valorEfetivado"),
                                status = runCatching {
                                    StatusCusto.valueOf(doc.getString("status") ?: "PENDENTE")
                                }.getOrDefault(StatusCusto.PENDENTE),
                                efetuadoPorSocioId = doc.getString("efetuadoPorSocioId"),
                                efetuadoPorSocioNome = doc.getString("efetuadoPorSocioNome"),
                                dataVencimento = doc.getLong("dataVencimento"),
                                dataEfetivacao = doc.getLong("dataEfetivacao"),
                                observacoes = doc.getString("observacoes") ?: ""
                            )
                        }.getOrNull()
                    }

                    _custos.value = _custos.value.filter { it.imovelId != imovelId } + custosNuvem
                }
            custosListeners[imovelId] = listener
        }
    }

    private fun carregarDadosIniciaisLocal() {
        val imovelExemplo = Imovel(
            id = "imovel_001",
            titulo = "Apto 3 Qts - Centro (Leilão Caixa)",
            rua = "Av. Afonso Pena",
            numero = "1500",
            bairro = "Centro",
            cidade = "Belo Horizonte",
            uf = "MG",
            apartamento = "802",
            condominio = "Edifício Afonso Pena",
            bancoOrigem = "CAIXA",
            status = StatusImovel.DESOCUPACAO,
            valorAvaliacao = 380000.0,
            valorMatricula = 320000.0,
            valorLanceArrematado = 195000.0,
            valorVendaPretendido = 340000.0
        )

        val custosExemplo = listOf(
            Custo(
                id = "custo_001",
                imovelId = imovelExemplo.id,
                titulo = "Valor do Lance de Arrematação",
                categoria = CategoriaCusto.ARREMATACAO,
                valorEstimado = 195000.0,
                valorEfetivado = 195000.0,
                status = StatusCusto.EFETIVADO,
                efetuadoPorSocioId = "socio_1",
                efetuadoPorSocioNome = "Sócio A (Elias)",
                dataEfetivacao = System.currentTimeMillis() - 864000000L,
                observacoes = "Pagamento da entrada do leilão Caixa"
            ),
            Custo(
                id = "custo_002",
                imovelId = imovelExemplo.id,
                titulo = "Comissão do Leiloeiro Oficial (5%)",
                categoria = CategoriaCusto.ARREMATACAO,
                valorEstimado = 9750.0,
                valorEfetivado = 9750.0,
                status = StatusCusto.EFETIVADO,
                efetuadoPorSocioId = "socio_2",
                efetuadoPorSocioNome = "Sócio B (Carlos)",
                dataEfetivacao = System.currentTimeMillis() - 800000000L
            ),
            Custo(
                id = "custo_003",
                imovelId = imovelExemplo.id,
                titulo = "IPTU Atrasado (Prefeitura)",
                categoria = CategoriaCusto.DEBITOS_ANTERIORES,
                valorEstimado = 4500.0,
                valorEfetivado = 4120.0,
                status = StatusCusto.EFETIVADO,
                efetuadoPorSocioId = "socio_2",
                efetuadoPorSocioNome = "Sócio B (Carlos)",
                dataEfetivacao = System.currentTimeMillis() - 400000000L,
                observacoes = "Desconto obtido no pagamento à vista da dívida ativa"
            ),
            Custo(
                id = "custo_004",
                imovelId = imovelExemplo.id,
                titulo = "Condomínio Acumulado Atrasado",
                categoria = CategoriaCusto.DEBITOS_ANTERIORES,
                valorEstimado = 6200.0,
                status = StatusCusto.PENDENTE
            ),
            Custo(
                id = "custo_005",
                imovelId = imovelExemplo.id,
                titulo = "Honorários Advocatícios (Imissão na Posse)",
                categoria = CategoriaCusto.JURIDICO_DESOCUPACAO,
                valorEstimado = 5000.0,
                valorEfetivado = 2500.0,
                status = StatusCusto.EFETIVADO,
                efetuadoPorSocioId = "socio_1",
                efetuadoPorSocioNome = "Sócio A (Elias)",
                dataEfetivacao = System.currentTimeMillis() - 300000000L,
                observacoes = "Primeira parcela dos honorários do advogado"
            ),
            Custo(
                id = "custo_006",
                imovelId = imovelExemplo.id,
                titulo = "Acordo de Desocupação (Ajuda de Custo)",
                categoria = CategoriaCusto.JURIDICO_DESOCUPACAO,
                valorEstimado = 3000.0,
                status = StatusCusto.PENDENTE
            ),
            Custo(
                id = "custo_007",
                imovelId = imovelExemplo.id,
                titulo = "ITBI (Imposto de Transmissão)",
                categoria = CategoriaCusto.CARTORIO_IMPOSTOS,
                valorEstimado = 5850.0,
                status = StatusCusto.PENDENTE
            ),
            Custo(
                id = "custo_008",
                imovelId = imovelExemplo.id,
                titulo = "Escritura / Carta de Arrematação & Matrícula",
                categoria = CategoriaCusto.CARTORIO_IMPOSTOS,
                valorEstimado = 3200.0,
                status = StatusCusto.PENDENTE
            ),
            Custo(
                id = "custo_009",
                imovelId = imovelExemplo.id,
                titulo = "Pintura Geral e Reparos Hidráulicos/Elétricos",
                categoria = CategoriaCusto.MANUTENCAO_REFORMA,
                valorEstimado = 8500.0,
                status = StatusCusto.PENDENTE
            ),
            Custo(
                id = "custo_010",
                imovelId = imovelExemplo.id,
                titulo = "Comissão de Corretagem da Venda (5%)",
                categoria = CategoriaCusto.VENDA_DESINVESTIMENTO,
                valorEstimado = 17000.0,
                status = StatusCusto.PENDENTE
            )
        )

        _imoveis.value = listOf(imovelExemplo)
        _custos.value = custosExemplo
    }

    fun getCustosPorImovel(imovelId: String): Flow<List<Custo>> {
        return _custos.map { list -> list.filter { it.imovelId == imovelId } }
    }

    fun salvarSocio(socio: Socio) {
        val existe = _socios.value.any { it.id == socio.id }
        if (existe) {
            _socios.value = _socios.value.map { if (it.id == socio.id) socio else it }
        } else {
            _socios.value = _socios.value + socio
        }

        val db = firestore ?: return
        runCatching {
            val socioMap = hashMapOf(
                "nome" to socio.nome,
                "email" to socio.email,
                "corHex" to socio.corHex
            )
            db.collection("socios").document(socio.id).set(socioMap)
        }
    }

    fun adicionarImovel(imovel: Imovel, custosIniciais: List<Custo>) {
        _imoveis.value = _imoveis.value + imovel
        _custos.value = _custos.value + custosIniciais

        val db = firestore ?: return
        runCatching {
            val imovelMap = hashMapOf(
                "titulo" to imovel.titulo,
                "endereco" to imovel.enderecoCompleto,
                "rua" to imovel.rua,
                "numero" to imovel.numero,
                "bairro" to imovel.bairro,
                "cidade" to imovel.cidade,
                "uf" to imovel.uf,
                "condominio" to imovel.condominio,
                "bloco" to imovel.bloco,
                "apartamento" to imovel.apartamento,
                "bancoOrigem" to imovel.bancoOrigem,
                "status" to imovel.status.name,
                "valorAvaliacao" to imovel.valorAvaliacao,
                "valorMatricula" to imovel.valorMatricula,
                "valorLanceArrematado" to imovel.valorLanceArrematado,
                "valorVendaPretendido" to imovel.valorVendaPretendido,
                "valorVendaRealizado" to imovel.valorVendaRealizado,
                "criadoEm" to imovel.criadoEm
            )
            db.collection("imoveis").document(imovel.id).set(imovelMap)

            custosIniciais.forEach { custo ->
                val custoMap = hashMapOf(
                    "imovelId" to custo.imovelId,
                    "titulo" to custo.titulo,
                    "categoria" to custo.categoria.name,
                    "valorEstimado" to custo.valorEstimado,
                    "valorEfetivado" to custo.valorEfetivado,
                    "status" to custo.status.name,
                    "efetuadoPorSocioId" to custo.efetuadoPorSocioId,
                    "efetuadoPorSocioNome" to custo.efetuadoPorSocioNome,
                    "dataVencimento" to custo.dataVencimento,
                    "dataEfetivacao" to custo.dataEfetivacao,
                    "observacoes" to custo.observacoes
                )
                db.collection("imoveis").document(imovel.id).collection("custos").document(custo.id).set(custoMap)
            }
        }
    }

    fun editarImovel(imovel: Imovel) {
        _imoveis.value = _imoveis.value.map { if (it.id == imovel.id) imovel else it }

        val db = firestore ?: return
        runCatching {
            val imovelMap = hashMapOf(
                "titulo" to imovel.titulo,
                "endereco" to imovel.enderecoCompleto,
                "rua" to imovel.rua,
                "numero" to imovel.numero,
                "bairro" to imovel.bairro,
                "cidade" to imovel.cidade,
                "uf" to imovel.uf,
                "condominio" to imovel.condominio,
                "bloco" to imovel.bloco,
                "apartamento" to imovel.apartamento,
                "bancoOrigem" to imovel.bancoOrigem,
                "status" to imovel.status.name,
                "valorAvaliacao" to imovel.valorAvaliacao,
                "valorMatricula" to imovel.valorMatricula,
                "valorLanceArrematado" to imovel.valorLanceArrematado,
                "valorVendaPretendido" to imovel.valorVendaPretendido,
                "valorVendaRealizado" to imovel.valorVendaRealizado,
                "criadoEm" to imovel.criadoEm
            )
            db.collection("imoveis").document(imovel.id).set(imovelMap)
        }
    }

    fun adicionarCusto(custo: Custo) {
        _custos.value = _custos.value + custo

        val db = firestore ?: return
        runCatching {
            val custoMap = hashMapOf(
                "imovelId" to custo.imovelId,
                "titulo" to custo.titulo,
                "categoria" to custo.categoria.name,
                "valorEstimado" to custo.valorEstimado,
                "valorEfetivado" to custo.valorEfetivado,
                "status" to custo.status.name,
                "efetuadoPorSocioId" to custo.efetuadoPorSocioId,
                "efetuadoPorSocioNome" to custo.efetuadoPorSocioNome,
                "dataVencimento" to custo.dataVencimento,
                "dataEfetivacao" to custo.dataEfetivacao,
                "observacoes" to custo.observacoes
            )
            db.collection("imoveis").document(custo.imovelId).collection("custos").document(custo.id).set(custoMap)
        }
    }

    fun removerCusto(custoId: String) {
        val custo = _custos.value.find { it.id == custoId } ?: return
        _custos.value = _custos.value.filter { it.id != custoId }

        val db = firestore ?: return
        runCatching {
            db.collection("imoveis")
                .document(custo.imovelId)
                .collection("custos")
                .document(custoId)
                .delete()
        }
    }

    fun efetivarCusto(
        custoId: String,
        valorEfetivado: Double,
        socioId: String,
        observacoes: String
    ) {
        val socio = _socios.value.find { it.id == socioId }
        val socioNome = socio?.nome ?: "Desconhecido"

        val custoAtual = _custos.value.find { it.id == custoId } ?: return

        val custoAtualizado = custoAtual.copy(
            valorEfetivado = valorEfetivado,
            status = StatusCusto.EFETIVADO,
            efetuadoPorSocioId = socioId,
            efetuadoPorSocioNome = socioNome,
            dataEfetivacao = System.currentTimeMillis(),
            observacoes = observacoes.ifBlank { custoAtual.observacoes }
        )

        _custos.value = _custos.value.map { if (it.id == custoId) custoAtualizado else it }

        val db = firestore ?: return
        runCatching {
            val updateMap = hashMapOf<String, Any?>(
                "valorEfetivado" to valorEfetivado,
                "status" to StatusCusto.EFETIVADO.name,
                "efetuadoPorSocioId" to socioId,
                "efetuadoPorSocioNome" to socioNome,
                "dataEfetivacao" to custoAtualizado.dataEfetivacao,
                "observacoes" to custoAtualizado.observacoes
            )
            db.collection("imoveis")
                .document(custoAtual.imovelId)
                .collection("custos")
                .document(custoId)
                .update(updateMap)
        }
    }

    fun calcularResumoFinanceiro(imovelId: String): Flow<ResumoFinanceiro?> {
        return combine(_imoveis, _custos, _socios) { imoveisList, custosList, sociosList ->
            val imovel = imoveisList.find { it.id == imovelId } ?: return@combine null
            val custosImovel = custosList.filter { it.imovelId == imovelId }

            val totalEstimado = custosImovel.sumOf { it.valorEstimado }
            val totalEfetivado = custosImovel.filter { it.status == StatusCusto.EFETIVADO }
                .sumOf { it.valorEfetivado ?: 0.0 }

            val custoTotalAtual = custosImovel.sumOf { it.valorFinal }

            val aportesPorSocio = sociosList.map { socio ->
                val totalPagoPeloSocio = custosImovel
                    .filter { it.status == StatusCusto.EFETIVADO && it.efetuadoPorSocioId == socio.id }
                    .sumOf { it.valorEfetivado ?: 0.0 }

                val percentual = if (totalEfetivado > 0) (totalPagoPeloSocio / totalEfetivado) * 100.0 else 0.0

                AporteSocio(
                    socio = socio,
                    totalPago = totalPagoPeloSocio,
                    percentualDoTotal = percentual
                )
            }

            val receitaPretendida = imovel.valorVendaPretendido
            val lucroProjetado = receitaPretendida - custoTotalAtual
            val roiProjetadoPercentual = if (custoTotalAtual > 0) (lucroProjetado / custoTotalAtual) * 100.0 else 0.0

            val lucroRealizado = imovel.valorVendaRealizado?.let { receitaRealizada ->
                receitaRealizada - totalEfetivado
            }

            ResumoFinanceiro(
                imovelId = imovelId,
                totalEstimado = totalEstimado,
                totalEfetivado = totalEfetivado,
                custoTotalAtual = custoTotalAtual,
                aportesPorSocio = aportesPorSocio,
                lucroProjetado = lucroProjetado,
                roiProjetadoPercentual = roiProjetadoPercentual,
                lucroRealizado = lucroRealizado
            )
        }
    }
}
