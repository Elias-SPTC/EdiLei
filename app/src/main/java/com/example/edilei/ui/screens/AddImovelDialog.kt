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
import com.example.edilei.utils.parseBrlToDouble
import com.example.edilei.utils.parseDataToLong

@Composable
fun AddImovelDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        titulo: String,
        rua: String,
        numero: String,
        bairro: String,
        cidade: String,
        uf: String,
        condominio: String,
        bloco: String,
        apartamento: String,
        banco: String,
        avaliacao: Double,
        matricula: Double,
        lance: Double,
        vendaPretendida: Double,
        dataArrematacao: Long?
    ) -> Unit
) {
    var tituloText by remember { mutableStateOf("") }
    var ruaText by remember { mutableStateOf("") }
    var numeroText by remember { mutableStateOf("") }
    var bairroText by remember { mutableStateOf("") }
    var cidadeText by remember { mutableStateOf("") }
    var ufText by remember { mutableStateOf("MG") }
    var condominioText by remember { mutableStateOf("") }
    var blocoText by remember { mutableStateOf("") }
    var apartamentoText by remember { mutableStateOf("") }

    var bancoText by remember { mutableStateOf("CAIXA") }
    var avaliacaoText by remember { mutableStateOf("") }
    var matriculaText by remember { mutableStateOf("") }
    var lanceText by remember { mutableStateOf("") }
    var vendaPretendidaText by remember { mutableStateOf("") }
    var dataArrematacaoText by remember { mutableStateOf("") }

    val isFormValid = tituloText.isNotBlank() &&
            ruaText.isNotBlank() &&
            avaliacaoText.isNotBlank() &&
            lanceText.isNotBlank() &&
            vendaPretendidaText.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cadastrar Imóvel de Leilão",
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
                    text = "Dados do Imóvel",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = tituloText,
                    onValueChange = { tituloText = it },
                    label = { Text("Identificação / Título (ex: Apt Centro)") },
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
                        label = { Text("Logradouro / Rua / Av.") },
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
                    label = { Text("Condomínio / Edifício (Opcional)") },
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
                    text = "Valores & Leilão",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = bancoText,
                    onValueChange = { bancoText = it },
                    label = { Text("Banco / Origem do Leilão") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = avaliacaoText,
                    onValueChange = { avaliacaoText = it },
                    label = { Text("Valor de Avaliação (R$) (ex: 380.000,00)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = matriculaText,
                    onValueChange = { matriculaText = it },
                    label = { Text("Valor de Matrícula / Venal (R$) (ex: 320.000,00)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = lanceText,
                    onValueChange = { lanceText = it },
                    label = { Text("Valor do Lance (R$) (ex: 195.000,00)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = vendaPretendidaText,
                    onValueChange = { vendaPretendidaText = it },
                    label = { Text("Valor Pretendido Venda (R$) (ex: 340.000,00)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Data do Evento",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                DatePickerField(
                    label = "Data de Arrematação / Compra",
                    value = dataArrematacaoText,
                    onValueChange = { dataArrematacaoText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = isFormValid,
                onClick = {
                    onConfirm(
                        tituloText,
                        ruaText,
                        numeroText,
                        bairroText,
                        cidadeText,
                        ufText,
                        condominioText,
                        blocoText,
                        apartamentoText,
                        bancoText,
                        avaliacaoText.parseBrlToDouble(),
                        matriculaText.parseBrlToDouble(),
                        lanceText.parseBrlToDouble(),
                        vendaPretendidaText.parseBrlToDouble(),
                        dataArrematacaoText.parseDataToLong()
                    )
                }
            ) {
                Text("Cadastrar & Gerar Checklist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
