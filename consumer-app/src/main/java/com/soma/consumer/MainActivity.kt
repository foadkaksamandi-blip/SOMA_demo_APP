package com.soma.consumer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.OutlinedTextField

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        ConsumerHome()
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumerHome() {
    val scroll = rememberScrollState()

    // موجودی‌ها (نسخه واحد: چهار کیف)
    var balanceMain by remember { mutableStateOf(10_000_000L) }
    var balanceSubsidy by remember { mutableStateOf(0L) }
    var balanceEmergency by remember { mutableStateOf(0L) }
    var balanceCBDC by remember { mutableStateOf(0L) }

    var amountInput by remember { mutableStateOf("") }

    // بنر وضعیت
    var bannerVisible by remember { mutableStateOf(false) }
    var bannerSuccess by remember { mutableStateOf(true) }
    var bannerText by remember { mutableStateOf("") }
    var bannerTxId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        // هدر
        Text(text = "آپ آفلاین سوما 👤", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(text = "اپ خریدار", fontSize = 16.sp)

        // بنر وضعیت
        AnimatedVisibility(visible = bannerVisible, enter = fadeIn(), exit = fadeOut()) {
            val color = if (bannerSuccess) androidx.compose.ui.graphics.Color(0xFF22C55E)
                        else androidx.compose.ui.graphics.Color(0xFFEF4444)
            Card(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(color)
                        .padding(12.dp)
                ) {
                    Text(
                        text = bannerText + (bannerTxId?.let { " | کد: $it" } ?: ""),
                        color = androidx.compose.ui.graphics.Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // کارت موجودی‌ها
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("موجودی کیف اصلی: ${"%,d".format(balanceMain)} تومان", fontWeight = FontWeight.Bold)
                Text("موجودی یارانه: ${"%,d".format(balanceSubsidy)} تومان")
                Text("موجودی اضطراری: ${"%,d".format(balanceEmergency)} تومان")
                Text("موجودی رمز ارز ملی: ${"%,d".format(balanceCBDC)} تومان")
            }
        }

        // ورودی مبلغ
        OutlinedTextField(
            value = amountInput,
            onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
            label = { Text("مبلغ (تومان)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // ردیف دکمه‌های اصلی
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    // پرداخت با BLE — اتصال واقعی در مرحله BLE افزوده می‌شود
                    val amt = amountInput.toLongOrNull() ?: 0L
                    if (amt > 0 && amt <= balanceMain) {
                        balanceMain -= amt
                        showBanner(success = true, text = "پرداخت انجام شد ✅", setVisible = { bannerVisible = it }) {
                            bannerSuccess = true
                            bannerText = "پرداخت انجام شد ✅"
                            bannerTxId = generateTxId()
                        }
                    } else {
                        showBanner(success = false, text = "مبلغ/موجودی نامعتبر", setVisible = { bannerVisible = it }) {
                            bannerSuccess = false
                            bannerText = "تراکنش ناموفق ❌"
                            bannerTxId = generateTxId()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
            ) { Text("پرداخت با بلوتوث", color = MaterialTheme.colorScheme.primary) }

            Button(
                onClick = {
                    // پرداخت با QR — اسکن واقعی در فاز QR اضافه می‌شود
                    val amt = amountInput.toLongOrNull() ?: 0L
                    if (amt > 0 && amt <= balanceMain) {
                        balanceMain -= amt
                        showBanner(true, "پرداخت انجام شد ✅", { bannerVisible = it }) {
                            bannerSuccess = true
                            bannerText = "پرداخت انجام شد ✅"
                            bannerTxId = generateTxId()
                        }
                    } else {
                        showBanner(false, "تراکنش ناموفق ❌", { bannerVisible = it }) {
                            bannerSuccess = false
                            bannerText = "تراکنش ناموفق ❌"
                            bannerTxId = generateTxId()
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
            ) { Text("پرداخت با QR کد", color = MaterialTheme.colorScheme.primary) }
        }

        // ردیف دوم
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { /* تاریخچه – در فاز Store پیاده‌سازی می‌شود */ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
            ) { Text("تاریخچه تراکنش‌ها", color = MaterialTheme.colorScheme.primary) }

            Button(
                onClick = { balanceMain += 100_000 },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
            ) { Text("شارژ آزمایشی +۱۰۰,۰۰۰", color = MaterialTheme.colorScheme.primary) }
        }

        // کارت‌های ویژه (UI آماده؛ منطق در فازهای بعد)
        SectionWalletCard(title = "یارانه ملی") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { /* BLE یارانه */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) { Text("پرداخت از یارانه", color = MaterialTheme.colorScheme.primary) }
                Button(
                    onClick = { /* QR یارانه */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) { Text("پرداخت با QR کد", color = MaterialTheme.colorScheme.primary) }
            }
        }
        SectionWalletCard(title = "اعتبار اضطراری ملی") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { /* BLE اضطراری */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) { Text("پرداخت اضطراری", color = MaterialTheme.colorScheme.primary) }
                Button(
                    onClick = { /* QR اضطراری */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) { Text("پرداخت با QR کد", color = MaterialTheme.colorScheme.primary) }
            }
        }
        SectionWalletCard(title = "رمز ارز ملی") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { /* BLE CBDC */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) { Text("پرداخت با رمز ارز ملی", color = MaterialTheme.colorScheme.primary) }
                Button(
                    onClick = { /* QR CBDC */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White)
                ) { Text("پرداخت با QR کد", color = MaterialTheme.colorScheme.primary) }
            }
        }
    }
}

@Composable
private fun SectionWalletCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

private fun showBanner(success: Boolean, text: String, setVisible: (Boolean) -> Unit, assign: () -> Unit) {
    assign()
    setVisible(true)
    // در نسخه ساده: زمان‌بندی مخفی‌سازی به عهده UI؛ در فاز بعد می‌توانیم با LaunchedEffect تاخیر بدهیم.
}

private fun generateTxId(): String {
    // تولید ساده‌ی txId (در فاز Shared/DateUtils نهایی‌تر می‌شود)
    val millis = System.currentTimeMillis()
    val rnd = (1000..9999).random()
    return "SOMA-$millis-$rnd"
}
