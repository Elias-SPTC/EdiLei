package com.example.edilei.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.edilei.data.model.Imovel
import com.example.edilei.data.model.StatusImovel
import com.example.edilei.utils.formatBrlSemSimbolo
import com.example.edilei.utils.parseBrlToDouble

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarImovelDialog(
    imovel: Imovel,
    onDismiss: () -> Unit,
    onConfirm: (Imovel) -> Unit
) {
    var tituloText by remember { mutableStateOf(imovel.titulo) }

    var ruaText by remember { mutableStateOf(imovel.rua) }
    var numeroText by remember { mutableStateOf(imovel.numero) }
    var bairroText by remember { mutableStateOf(imovel.bairro) }
    var cidadeText by remember { mutableStateOf(imovel.cidade) }
    var ufText by remember { mutableStateOf(imovel.uf) }
    var condominioText by remember { mutableStateOf(imovel.condominio) }
    var blocoText by remember { mutableStateOf(imovel.bloco) }
    var apartamentoText by remember { mutableStateOf(imovel.apartamento) }

    var bancoText by remember { mutableStateOf(imovel.bancoOrigem) }
    var selectedStatus by remember { mutableStateOf(imovel.status) }
    var avaliacaoText by remember { mutableStateOf(imovel.valorAvaliacao.formatBrlSemSimbolo()) }
    var matriculaText by remember { mutableStateOf(imovel.valorMatricula.formatBrlSemSimbolo()) }
    var lanceText by remember { mutableStateOf(imovel.valorLanceArrematado.formatBrlSemSimbolo()) }
    var vendaPretendidaText by remember { mutableStateOf(imovel.valorVendaPretendido.formatBrlSemSimbolo()) }
    var vendaRealizadaText by remember { mutableStateOf(imovel.valorVendaRealizado?.formatBrlSemSimbolo() ?: "") }

    var expandedStatusDropdown by remember { mutableStateOf(false) }

    val isFormValid = tituloText.isNotBlank() &&
            avaliacaoText.isNotBlank() &&
            lanceText.isNotBlank() &&
            vendaPretendidaText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar Imóvel",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Identificação",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = tituloText,
                    onValueChange = { tituloText = it },
                    label = { Text("Identificação / Título") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Endereço Detalhado",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = ruaText,
                        onValueChange = { ruaText = it },
                        label = { Text("Logradouro / Rua") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = numeroText,
                        onValueChange = { numeroText = it },
                        label = { Text("Nº") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = apartamentoText,
                        onValueChange = { apartamentoText = it },
                        label = { Text("Apt / Unidade") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = blocoText,
                        onValueChange = { blocoText = it },
                        label = { Text("Bloco") },
                        modifier = Modifier.weight(0.5f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = condominioText,
                    onValueChange = { condominioText = it },
                    label = { Text("Condomínio / Edifício") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = bairroText,
                    onValueChange = { bairroText = it },
                    label = { Text("Bairro") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = cidadeText,
                        onValueChange = { cidadeText = it },
                        label = { Text("Cidade") },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = ufText,
                        onValueChange = { ufText = it },
                        label = { Text("UF") },
                        modifier = Modifier.weight(0.3f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Valores & Status no Leilão",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = bancoText,
                    onValueChange = { bancoText = it },
                    label = { Text("Banco / Origem") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedStatusDropdown,
                    onExpandedChange = { expandedStatusDropdown = !expandedStatusDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedStatus.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Status do Imóvel") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatusDropdown) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedStatusDropdown,
                        onDismissRequest = { expandedStatusDropdown = false }
                    ) {
                        StatusImovel.entries.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.label) },
                                onClick = {
                                    selectedStatus = st
                                    expandedStatusDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = avaliacaoText,
                    onValueChange = { avaliacaoText = it },
                    label = { Text("Valor de Avaliação (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = matriculaText,
                    onValueChange = { matriculaText = it },
                    label = { Text("Valor de Matrícula / Venal (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lanceText,
                    onValueChange = { lanceText = it },
                    label = { Text("Valor do Lance (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vendaPretendidaText,
                    onValueChange = { vendaPretendidaText = it },
                    label = { Text("Venda Pretendida (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vendaRealizadaText,
                    onValueChange = { vendaRealizadaText = it },
                    label = { Text("Venda Realizada Final (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isFormValid,
                onClick = {
                    val imovelAtualizado = imovel.copy(
                        titulo = tituloText,
                        rua = ruaText,
                        numero = numeroText,
                        bairro = bairroText,
                        cidade = cidadeText,
                        uf = ufText,
                        condominio = condominioText,
                        bloco = blocoText,
                        apartamento = apartamentoText,
                        bancoOrigem = bancoText,
                        status = selectedStatus,
                        valorAvaliacao = avaliacaoText.parseBrlToDouble(),
                        valorMatricula = matriculaText.parseBrlToDouble(),
                        valorLanceArrematado = lanceText.parseBrlToDouble(),
                        valorVendaPretendido = vendaPretendidaText.parseBrlToDouble(),
                        valorVendaRealizado = if (vendaRealizadaText.isNotBlank()) vendaRealizadaText.parseBrlToDouble() else null
                    )
                    onConfirm(imovelAtualizado)
                }
            ) {
                Text("Salvar Alterações")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
