package io.xps.playground.ui.feature.inputs

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.theme.PlaygroundTheme

@AndroidEntryPoint
class InputsFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)
    private val viewModel by viewModels<InputsViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            val textWithTransformation by viewModel.textWithTransformation.collectAsState()
            PlaygroundTheme {
                InputsScreen(textWithTransformation)
            }
        }
    }

    @Composable
    fun InputsScreen(textWithTransformation: String) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VisualTransformationDemo(textWithTransformation)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun VisualTransformationDemo(textWithTransformation: String) {
        var otpValue by remember { mutableStateOf("") }
        OtpTextField(
            otpText = otpValue,
            onOtpTextChange = { value, _ ->
                otpValue = value
            }
        )

        TextField(
            value = textWithTransformation,
            visualTransformation = SampleVisualTransformation(),
            onValueChange = viewModel::input
        )
        Text(text = textWithTransformation, modifier = Modifier.imePadding())
    }

    private class SampleVisualTransformation : VisualTransformation {

        override fun filter(text: AnnotatedString): TransformedText {
            var result = ""
            text.forEachIndexed { i, s ->
                when (i) {
                    4 -> result += "-$s"
                    8 -> result += "-$s"
                    else -> result += s
                }
            }

            val creditCardOffsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    if (offset <= 4) return offset
                    if (offset <= 8) return offset + 1
                    if (offset <= 12) return offset + 2
                    return offset
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return offset
                }
            }

            return TransformedText(
                text = AnnotatedString(result),
                offsetMapping = creditCardOffsetMapping
            )
        }
    }

    @Composable
    fun OtpTextField(
        modifier: Modifier = Modifier,
        otpText: String,
        otpCount: Int = 6,
        onOtpTextChange: (String, Boolean) -> Unit
    ) {
        BasicTextField(
            modifier = modifier,
            value = otpText,
            onValueChange = {
                if (it.length <= otpCount) {
                    onOtpTextChange.invoke(it, it.length == otpCount)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            decorationBox = {
                Row(horizontalArrangement = Arrangement.Center) {
                    repeat(otpCount) { index ->
                        CharView(
                            index = index,
                            text = otpText
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        )
    }

    @Composable
    private fun CharView(index: Int, text: String) {
        val isFocused = text.length == index
        val char = when {
            index == text.length -> "0"
            index > text.length -> ""
            else -> text[index].toString()
        }
        Text(
            modifier = Modifier
                .width(40.dp)
                .border(
                    1.dp,
                    when {
                        isFocused -> Color.DarkGray
                        else -> Color.LightGray
                    },
                    RoundedCornerShape(8.dp)
                )
                .padding(2.dp),
            text = char,
            style = MaterialTheme.typography.headlineLarge,
            color = if (isFocused) {
                Color.LightGray
            } else {
                Color.DarkGray
            },
            textAlign = TextAlign.Center
        )
    }
}
