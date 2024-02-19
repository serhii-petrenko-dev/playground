package io.xps.playground.ui.feature.serialization

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import io.xps.playground.R
import io.xps.playground.databinding.FragmentComposeBinding
import io.xps.playground.tools.TAG
import io.xps.playground.tools.fromJson
import io.xps.playground.tools.toJson
import io.xps.playground.tools.viewBinding
import io.xps.playground.ui.theme.PlaygroundTheme
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@AndroidEntryPoint
class SerializationFragment : Fragment(R.layout.fragment_compose) {

    private val binding by viewBinding(FragmentComposeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.containerCompose.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        binding.containerCompose.setContent {
            PlaygroundTheme {
                SerializationScreen()
            }
        }

        val json = provideJson()
        decodeTest(json)
        encodeTest(json)
    }

    @Composable
    fun SerializationScreen() {
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun provideJson() = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        encodeDefaults = true
        explicitNulls = true
        prettyPrint = true
        isLenient = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun decodeTest(json: Json) {
        Log.d(TAG, "decode test")

        Log.d(TAG, "decode CorrectFile ${fromJson<File>(json, fakeCorrectFileJson())}")

        try {
            Log.d(TAG, "decode NoIdFile ${fromJson<File>(json, fakeNoIdFileJson())}")
        } catch (e: MissingFieldException) {
            Log.d(TAG, "decode NoIdFile ${e.message}")
        }
        try {
            Log.d(TAG, "decode NullIdFile ${fromJson<File>(json, fakeNullIdFileJson())}")
        } catch (e: MissingFieldException) {
            Log.d(TAG, "decode NullIdFile ${e.message}")
        }

        Log.d(TAG, "decode NoNameFile ${fromJson<File>(json, fakeNoNameFileJson())}")
        Log.d(TAG, "decode NullNameFile ${fromJson<File>(json, fakeNullNameFileJson())}")

        Log.d(TAG, "decode NoParentFile ${fromJson<File>(json, fakeNoParentFileJson())}")
        Log.d(TAG, "decode NullParentFile ${fromJson<File>(json, fakeNullParentFileJson())}")

        Log.d(TAG, "decode NullSizeFile ${fromJson<File>(json, fakeNullSizeFileJson())}")

        try {
            Log.d(TAG, "decode WrongSizeType ${fromJson<File>(json, fakeWrongSizeTypeFileJson())}")
        } catch (e: SerializationException) {
            Log.d(TAG, "decode WrongSizeType ${e.message}")
        }

        Log.d(TAG, "decode NoTypeFile ${fromJson<File>(json, fakeNoTypeFileJson())}")
        Log.d(
            TAG,
            "decode SerialNameTypeFile ${fromJson<File>(json, fakeSerialNameTypeFileJson())}"
        )
        Log.d(TAG, "decode UnknownTypeFile ${fromJson<File>(json, fakeUnknownTypeFileJson())}")
    }

    private fun encodeTest(json: Json) {
        Log.d(TAG, "encode test")

        val file0 = fromJson<File>(json, fakeCorrectFileJson())
        Log.d(TAG, "encode CorrectFile ${file0.toJson(json)}")

        val file1 = fromJson<File>(json, fakeNoNameFileJson())
        Log.d(TAG, "encode NoNameFile ${file1.toJson(json)}")
        val file2 = fromJson<File>(json, fakeNullNameFileJson())
        Log.d(TAG, "encode NullNameFile ${file2.toJson(json)}")

        val file3 = fromJson<File>(json, fakeNoParentFileJson())
        Log.d(TAG, "encode NoParentFile ${file3.toJson(json)}")
        val file4 = fromJson<File>(json, fakeNullParentFileJson())
        Log.d(TAG, "encode NullParentFile ${file4.toJson(json)}")
    }

    private fun fakeCorrectFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "PDF")
    }

    private fun fakeNoIdFileJson() = buildJsonObject {
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "PDF")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun fakeNullIdFileJson() = buildJsonObject {
        put("id", null)
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "PDF")
    }

    private fun fakeNoNameFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "PDF")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun fakeNullNameFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", null)
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "PDF")
    }

    private fun fakeNoParentFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("size", 14.0)
        put("type", "PDF")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun fakeNullParentFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", null)
        put("size", 14.0)
        put("type", "PDF")
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun fakeNullSizeFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", null)
        put("type", "PDF")
    }

    private fun fakeWrongSizeTypeFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", "fourteen")
        put("type", "PDF")
    }

    private fun fakeNoTypeFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", 14.0)
    }

    private fun fakeSerialNameTypeFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "executable")
    }

    private fun fakeUnknownTypeFileJson() = buildJsonObject {
        put("id", "random_file_id")
        put("name", "file_name")
        put("parent", "file_parent")
        put("size", 14.0)
        put("type", "doc")
    }
}
