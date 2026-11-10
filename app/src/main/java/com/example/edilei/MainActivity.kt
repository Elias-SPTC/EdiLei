package com.example.edilei

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.edilei.data.model.Imovel
import com.example.edilei.ui.screens.AddImovelDialog
import com.example.edilei.ui.screens.DashboardScreen
import com.example.edilei.ui.screens.EditarImovelDialog
import com.example.edilei.ui.screens.ImovelDetailScreen
import com.example.edilei.ui.theme.EdiLeiTheme
import com.example.edilei.ui.viewmodel.ImovelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EdiLeiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EdiLeiApp()
                }
            }
        }
    }
}

@Composable
fun EdiLeiApp(
    viewModel: ImovelViewModel = remember { ImovelViewModel() }
) {
    val imoveis by viewModel.imoveis.collectAsState()
    val socios by viewModel.socios.collectAsState()
    val selectedImovelId by viewModel.selectedImovelId.collectAsState()
    val currentSocioId by viewModel.currentSocioId.collectAsState()
    val custos by viewModel.custosDoImovelSelecionado.collectAsState()
    val resumoFinanceiro by viewModel.resumoFinanceiro.collectAsState()

    var showAddImovelDialog by remember { mutableStateOf(false) }
    var imovelParaEditar by remember { mutableStateOf<Imovel?>(null) }

    val imovelSelecionado = imoveis.find { it.id == selectedImovelId }

    if (imovelSelecionado != null) {
        ImovelDetailScreen(
            imovel = imovelSelecionado,
            custos = custos,
            resumoFinanceiro = resumoFinanceiro,
            socios = socios,
            socioAtualId = currentSocioId,
            onBack = { viewModel.selecionarImovel(null) },
            onEditarImovel = { imovelEditado -> viewModel.editarImovel(imovelEditado) },
            onEfetivarCusto = { custoId, valorEfetivado, socioId, observacoes ->
                viewModel.efetivarCusto(custoId, valorEfetivado, socioId, observacoes)
            },
            onAdicionarCusto = { titulo, categoria, valor ->
                viewModel.adicionarCusto(titulo, categoria, valor)
            },
            onRemoverCusto = { custoId ->
                viewModel.removerCusto(custoId)
            }
        )
    } else {
        DashboardScreen(
            imoveis = imoveis,
            socios = socios,
            socioAtualId = currentSocioId,
            onSelectSocio = { socioId -> viewModel.selecionarSocioAtual(socioId) },
            onSelectImovel = { imovel -> viewModel.selecionarImovel(imovel.id) },
            onEditarImovelClick = { imovel -> imovelParaEditar = imovel },
            onAdicionarImovelClick = { showAddImovelDialog = true },
            onSalvarSocio = { id, nome, email, cor -> viewModel.salvarSocio(id, nome, email, cor) }
        )
    }

    if (showAddImovelDialog) {
        AddImovelDialog(
            onDismiss = { showAddImovelDialog = false },
            onConfirm = { titulo, rua, numero, bairro, cidade, uf, condominio, bloco, apartamento, banco, avaliacao, matricula, lance, venda ->
                viewModel.adicionarImovel(
                    titulo = titulo,
                    rua = rua,
                    numero = numero,
                    bairro = bairro,
                    cidade = cidade,
                    uf = uf,
                    condominio = condominio,
                    bloco = bloco,
                    apartamento = apartamento,
                    bancoOrigem = banco,
                    valorAvaliacao = avaliacao,
                    valorMatricula = matricula,
                    valorLanceArrematado = lance,
                    valorVendaPretendido = venda
                )
                showAddImovelDialog = false
            }
        )
    }

    imovelParaEditar?.let { imovel ->
        EditarImovelDialog(
            imovel = imovel,
            onDismiss = { imovelParaEditar = null },
            onConfirm = { imovelEditado ->
                viewModel.editarImovel(imovelEditado)
                imovelParaEditar = null
            }
        )
    }
}
