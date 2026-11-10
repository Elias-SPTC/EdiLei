package com.example.edilei.data.repository

import android.content.Context
import com.example.edilei.EdiLeilaoApplication
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
import org.json.JSONArray
import org.json.JSONObject

class ImovelRepository {

    private val firestore: FirebaseFirestore? by lazy {
        runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    }

    private val prefs by lazy {
        EdiLeilaoApplication.instance.getSharedPreferences("edilei_local_cache", Context.MODE_PRIVATE)
    }

    private val _socios = MutableStateFlow<List<Socio>>(emptyList())
    val socios: StateFlow<List<Socio>> = _socios.asStateFlow()

    private val _imoveis = MutableStateFlow<List<Imovel>>(emptyList())
    val imoveis: StateFlow<List<Imovel>> = _imoveis.asStateFlow()

    private val _custos = MutableStateFlow<List<Custo>>(emptyList())
    val custos: StateFlow<List<Custo>> = _custos.asStateFlow()

    private var sociosListener: ListenerRegistration? = null
    private var imoveisListener: ListenerRegistration? = null
    private val custosListeners = mutableMapOf<String, ListenerRegistration>()

    init {
        carregarCacheLocal()
        iniciarSincronizacaoFirestore()
    }

    private fun carregarCacheLocal() {
        runCatching {
            val sociosJson = prefs.getString("cache_socios", null)
            if (sociosJson != null) {
                val jsonArray = JSONArray(sociosJson)
                val lista = mutableListOf<Socio>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    lista.add(
                        Socio(
                            id = obj.getString("id"),
                            nome = obj.getString("nome"),
                            email = obj.getString("email"),
                            corHex = obj.optString("corHex", "#1E88E5")
                        )
                    )
                }
                if (lista.isNotEmpty()) _socios.value = lista
            } else {
                val padrao = listOf(
                    Socio("socio_1", "Elias do Carmo Corrêdo", "elias@empresa.com", "#1E88E5"),
                    Socio("socio_2", "Edilma Silva do Nascimento", "edilma@empresa.com", "#43A047")
                )
                _socios.value = padrao
                salvarCacheSocios(padrao)
            }

            val imoveisJson = prefs.getString("cache_imoveis", null)
            if (imoveisJson != null) {
                val jsonArray = JSONArray(imoveisJson)
                val lista = mutableListOf<Imovel>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    lista.add(
                        Imovel(
                            id = obj.getString("id"),
                            titulo = obj.getString("titulo"),
                            endereco = obj.optString("endereco", ""),
                            rua = obj.optString("rua", ""),
                            numero = obj.optString("numero", ""),
                            bairro = obj.optString("bairro", ""),
                            cidade = obj.optString("cidade", ""),
                            uf = obj.optString("uf", "MG"),
                            condominio = obj.optString("condominio", ""),
                            bloco = obj.optString("bloco", ""),
                            apartamento = obj.optString("apartamento", ""),
                            bancoOrigem = obj.optString("bancoOrigem", "CAIXA"),
                            status = runCatching { StatusImovel.valueOf(obj.getString("status")) }.getOrDefault(StatusImovel.ARREMATADO),
                            valorAvaliacao = obj.optDouble("valorAvaliacao", 0.0),
                            valorMatricula = obj.optDouble("valorMatricula", 0.0),
                            valorLanceArrematado = obj.optDouble("valorLanceArrematado", 0.0),
                            valorVendaPretendido = obj.optDouble("valorVendaPretendido", 0.0),
                            valorVendaRealizado = if (obj.has("valorVendaRealizado") && !obj.isNull("valorVendaRealizado")) obj.getDouble("valorVendaRealizado") else null,
                            criadoEm = obj.optLong("criadoEm", System.currentTimeMillis())
                        )
                    )
                }
                if (lista.isNotEmpty()) _imoveis.value = lista
            }

            val custosJson = prefs.getString("cache_custos", null)
            if (custosJson != null) {
                val jsonArray = JSONArray(custosJson)
                val lista = mutableListOf<Custo>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    lista.add(
                        Custo(
                            id = obj.getString("id"),
                            imovelId = obj.getString("imovelId"),
                            titulo = obj.getString("titulo"),
                            categoria = runCatching { CategoriaCusto.valueOf(obj.getString("categoria")) }.getOrDefault(CategoriaCusto.MANUTENCAO_REFORMA),
                            valorEstimado = obj.optDouble("valorEstimado", 0.0),
                            valorEfetivado = if (obj.has("valorEfetivado") && !obj.isNull("valorEfetivado")) obj.getDouble("valorEfetivado") else null,
                            status = runCatching { StatusCusto.valueOf(obj.getString("status")) }.getOrDefault(StatusCusto.PENDENTE),
                            efetuadoPorSocioId = obj.optString("efetuadoPorSocioId").takeIf { it.isNotBlank() },
                            efetuadoPorSocioNome = obj.optString("efetuadoPorSocioNome").takeIf { it.isNotBlank() },
                            dataVencimento = if (obj.has("dataVencimento") && !obj.isNull("dataVencimento")) obj.getLong("dataVencimento") else null,
                            dataEfetivacao = if (obj.has("dataEfetivacao") && !obj.isNull("dataEfetivacao")) obj.getLong("dataEfetivacao") else null,
                            observacoes = obj.optString("observacoes", "")
                        )
                    )
                }
                if (lista.isNotEmpty()) _custos.value = lista
            }
        }
    }

    private fun salvarCacheSocios(lista: List<Socio>) {
        runCatching {
            val jsonArray = JSONArray()
            lista.forEach { s ->
                val obj = JSONObject().apply {
                    put("id", s.id)
                    put("nome", s.nome)
                    put("email", s.email)
                    put("corHex", s.corHex)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("cache_socios", jsonArray.toString()).apply()
        }
    }

    private fun salvarCacheImoveis(lista: List<Imovel>) {
        runCatching {
            val jsonArray = JSONArray()
            lista.forEach { im ->
                val obj = JSONObject().apply {
                    put("id", im.id)
                    put("titulo", im.titulo)
                    put("endereco", im.endereco)
                    put("rua", im.rua)
                    put("numero", im.numero)
                    put("bairro", im.bairro)
                    put("cidade", im.cidade)
                    put("uf", im.uf)
                    put("condominio", im.condominio)
                    put("bloco", im.bloco)
                    put("apartamento", im.apartamento)
                    put("bancoOrigem", im.bancoOrigem)
                    put("status", im.status.name)
                    put("valorAvaliacao", im.valorAvaliacao)
                    put("valorMatricula", im.valorMatricula)
                    put("valorLanceArrematado", im.valorLanceArrematado)
                    put("valorVendaPretendido", im.valorVendaPretendido)
                    if (im.valorVendaRealizado != null) put("valorVendaRealizado", im.valorVendaRealizado)
                    put("criadoEm", im.criadoEm)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("cache_imoveis", jsonArray.toString()).apply()
        }
    }

    private fun salvarCacheCustos(lista: List<Custo>) {
        runCatching {
            val jsonArray = JSONArray()
            lista.forEach { c ->
                val obj = JSONObject().apply {
                    put("id", c.id)
                    put("imovelId", c.imovelId)
                    put("titulo", c.titulo)
                    put("categoria", c.categoria.name)
                    put("valorEstimado", c.valorEstimado)
                    if (c.valorEfetivado != null) put("valorEfetivado", c.valorEfetivado)
                    put("status", c.status.name)
                    if (c.efetuadoPorSocioId != null) put("efetuadoPorSocioId", c.efetuadoPorSocioId)
                    if (c.efetuadoPorSocioNome != null) put("efetuadoPorSocioNome", c.efetuadoPorSocioNome)
                    if (c.dataVencimento != null) put("dataVencimento", c.dataVencimento)
                    if (c.dataEfetivacao != null) put("dataEfetivacao", c.dataEfetivacao)
                    put("observacoes", c.observacoes)
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString("cache_custos", jsonArray.toString()).apply()
        }
    }

    private fun iniciarSincronizacaoFirestore() {
        val db = firestore ?: return

        sincronizarDadosLocaisParaNuvem()

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
                    salvarCacheSocios(listaSociosNuvem)
                } else {
                    _socios.value.forEach { socio ->
                        db.collection("socios").document(socio.id).set(
                            mapOf("nome" to socio.nome, "email" to socio.email, "corHex" to socio.corHex)
                        )
                    }
                }
            }
        }

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
                    salvarCacheImoveis(listaNuvem)
                    listaNuvem.forEach { imovel -> observarCustosDoImovelNuvem(imovel.id) }
                }
            }
        }
    }

    private fun sincronizarDadosLocaisParaNuvem() {
        val db = firestore ?: return
        runCatching {
            _imoveis.value.forEach { imovel ->
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

                _custos.value.filter { it.imovelId == imovel.id }.forEach { custo ->
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

                    val outrosCustos = _custos.value.filter { it.imovelId != imovelId }
                    val novaLista = outrosCustos + custosNuvem
                    _custos.value = novaLista
                    salvarCacheCustos(novaLista)
                }
            custosListeners[imovelId] = listener
        }
    }

    fun getCustosPorImovel(imovelId: String): Flow<List<Custo>> {
        return _custos.map { list -> list.filter { it.imovelId == imovelId } }
    }

    fun salvarSocio(socio: Socio) {
        val existe = _socios.value.any { it.id == socio.id }
        val novaLista = if (existe) {
            _socios.value.map { if (it.id == socio.id) socio else it }
        } else {
            _socios.value + socio
        }
        _socios.value = novaLista
        salvarCacheSocios(novaLista)

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
        val novaListaImoveis = _imoveis.value + imovel
        val novaListaCustos = _custos.value + custosIniciais
        _imoveis.value = novaListaImoveis
        _custos.value = novaListaCustos
        salvarCacheImoveis(novaListaImoveis)
        salvarCacheCustos(novaListaCustos)

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
        val novaLista = _imoveis.value.map { if (it.id == imovel.id) imovel else it }
        _imoveis.value = novaLista
        salvarCacheImoveis(novaLista)

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

    fun removerImovel(imovelId: String) {
        val novaListaImoveis = _imoveis.value.filter { it.id != imovelId }
        val novaListaCustos = _custos.value.filter { it.imovelId != imovelId }
        _imoveis.value = novaListaImoveis
        _custos.value = novaListaCustos
        salvarCacheImoveis(novaListaImoveis)
        salvarCacheCustos(novaListaCustos)

        val db = firestore ?: return
        runCatching {
            db.collection("imoveis").document(imovelId).delete()
        }
    }

    fun adicionarCusto(custo: Custo) {
        val novaLista = _custos.value + custo
        _custos.value = novaLista
        salvarCacheCustos(novaLista)

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

    fun editarCusto(custoId: String, novoTitulo: String, novoValorEstimado: Double) {
        val custoAtual = _custos.value.find { it.id == custoId } ?: return
        val custoAtualizado = custoAtual.copy(
            titulo = novoTitulo,
            valorEstimado = novoValorEstimado
        )
        val novaLista = _custos.value.map { if (it.id == custoId) custoAtualizado else it }
        _custos.value = novaLista
        salvarCacheCustos(novaLista)

        val db = firestore ?: return
        runCatching {
            val updateMap = hashMapOf<String, Any?>(
                "titulo" to novoTitulo,
                "valorEstimado" to novoValorEstimado
            )
            db.collection("imoveis")
                .document(custoAtual.imovelId)
                .collection("custos")
                .document(custoId)
                .update(updateMap)
        }
    }

    fun removerCusto(custoId: String) {
        val custo = _custos.value.find { it.id == custoId } ?: return
        val novaLista = _custos.value.filter { it.id != custoId }
        _custos.value = novaLista
        salvarCacheCustos(novaLista)

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

        val novaLista = _custos.value.map { if (it.id == custoId) custoAtualizado else it }
        _custos.value = novaLista
        salvarCacheCustos(novaLista)

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

            val receitaPretendida = imovel.valorVendaPretendido
            val lucroProjetado = receitaPretendida - custoTotalAtual
            val roiProjetadoPercentual = if (custoTotalAtual > 0) (lucroProjetado / custoTotalAtual) * 100.0 else 0.0

            val numSocios = if (sociosList.isNotEmpty()) sociosList.size else 1
            val lucroPorSocioIgual = lucroProjetado / numSocios

            val aportesPorSocio = sociosList.map { socio ->
                val totalPagoPeloSocio = custosImovel
                    .filter { it.status == StatusCusto.EFETIVADO && it.efetuadoPorSocioId == socio.id }
                    .sumOf { it.valorEfetivado ?: 0.0 }

                val percentual = if (totalEfetivado > 0) (totalPagoPeloSocio / totalEfetivado) * 100.0 else (100.0 / numSocios)

                AporteSocio(
                    socio = socio,
                    totalPago = totalPagoPeloSocio,
                    percentualDoTotal = percentual,
                    lucroProjetadoSocio = lucroPorSocioIgual
                )
            }

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
