package com.funny.compose.calclator.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

/**
 * @author  FunnySaltyFish
 * @date    2022/8/11 10:36
 */

@Composable
fun CalcScreen() {
    val systemUiController = rememberSystemUiController()
    val isDark = isSystemInDarkTheme()
    LaunchedEffect(systemUiController){
        systemUiController.isSystemBarsVisible = false
        systemUiController.setStatusBarColor(Color.Transparent, !isDark)
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)) { // 小于720dp当竖屏
        if (constraints.maxWidth / LocalDensity.current.density < 720) {
            CalcScreenVertical(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp))
        } else { // 否则当横屏
            CalcScreenHorizontal(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp))
        }
    }
}

@Composable
fun CalcScreenVertical(modifier: Modifier) {
    val vm: CalcViewModel = viewModel()
    SubcomposeBottomFirstLayout(modifier, other = {
        SubcomposeBottomFirstLayout(modifier = Modifier.padding(end = 8.dp, bottom = 8.dp), other = {
            CalcHistory(modifier = Modifier.fillMaxWidth())
        }, bottom = {
            CalcText(modifier = Modifier.fillMaxWidth(), formulaTextProvider = { vm.formulaText.ifBlank { "0" } }, resultTextProvider = { vm.resultText })
        })
    }, bottom = {
        CalcInput(symbols = vm.symbols)
    })
}

@Composable
fun CalcInput(
    modifier: Modifier = Modifier,
    symbols: Array<CharArray>,
    isVertical: Boolean = true,
) {
    val vm: CalcViewModel = viewModel()
    var l by remember {
        mutableStateOf(0)
    }
    Box(
        Modifier
            .layout { measurable, constraints ->
                val w: Int
                val h: Int
                if (isVertical) {
                    w = constraints.maxWidth
                    l = w / symbols[0].size
                    h = l * symbols.size
                } else {
                    h = constraints.maxHeight
                    l = h / symbols.size
                    w = l * symbols[0].size
                }
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = w,
                        maxWidth = w,
                        minHeight = h,
                        maxHeight = h
                    )
                )
                layout(w, h) {
                    placeable.placeRelative(0, 0)
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .border(2.dp, MaterialTheme.colors.onBackground.copy(0.8f), RoundedCornerShape(8.dp))) {
        symbols.forEachIndexed { i, array ->
            array.forEachIndexed { j, char ->
                Box(modifier = Modifier
                    .offset { IntOffset(j * l, i * l) }
                    .size(with(LocalDensity.current) { l.toDp() })
                    .padding(16.dp)
                    .clickable {
                        vm.click(char)
                    }) {
                    Text(modifier = Modifier.align(Alignment.Center), text = char.toString(), fontSize = 24.sp, color = contentColorFor(backgroundColor = MaterialTheme.colors.background))
                }
            }
        }
    }
}

@Composable
fun CalcScreenHorizontal(modifier: Modifier) {
    val vm: CalcViewModel = viewModel()
    Row(modifier) {
        CalcInput(modifier = Modifier, symbols = vm.symbolsHorizontal, false)

        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp, bottom = 8.dp)) {
            CalcText(modifier = Modifier.fillMaxWidth(), formulaTextProvider = { vm.formulaText.ifBlank { "0" } }, resultTextProvider = { vm.resultText })
            CalcHistory(modifier = Modifier)
        }
    }
}


@Composable
fun CalcHistory(modifier: Modifier) {
    val vm: CalcViewModel = viewModel()
    val listState = rememberLazyListState()
    LaunchedEffect(vm.histories.size) {
        if (vm.histories.isNotEmpty()) { // delay(550)
            listState.animateScrollToItem(vm.histories.size - 1)
        }
    }
    LazyColumn(modifier, state = listState) {
        items(vm.histories) { item ->
            val offset = remember { Animatable(100f) }
            LaunchedEffect(Unit) {
                offset.animateTo(0f)
            }
            Text(modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.value.toInt(), 0) }
                .padding(vertical = 4.dp), text = "${item.first} = ${item.second}", fontSize = 18.sp, color = MaterialTheme.colors.onBackground.copy(0.45f), textAlign = TextAlign.End)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CalcText(
    modifier: Modifier,
    formulaTextProvider: () -> String,
    resultTextProvider: () -> String,
) {
    val animSpec = remember {
        TweenSpec<Float>(500)
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Bottom) {
        val progressAnim = remember {
            Animatable(1f, 1f)
        } // 进度，1为仅有算式，0为结果
        val progress by remember { derivedStateOf { progressAnim.value } }
        Text(text = formulaTextProvider(), fontSize = (18 + 18 * progress).sp, color = MaterialTheme.colors.onBackground.copy((0.5f + 0.5f * progress)), textAlign = TextAlign.End)

        val resultText = resultTextProvider()
        if (resultText != "") {
            Text(text = resultText, fontSize = (36 - 18 * progress).sp, color = MaterialTheme.colors.onBackground.copy((1 - 0.5f * progress)), textAlign = TextAlign.End)
        }

        LaunchedEffect(resultText) {
            if (resultText != "") progressAnim.animateTo(0f, animationSpec = animSpec)
            else progressAnim.animateTo(1f, animationSpec = animSpec)
        }
    }
}

@Composable
fun SubcomposeBottomFirstLayout(modifier: Modifier, bottom: @Composable () -> Unit, other: @Composable () -> Unit) {
    val TAG = "BottomFirstLayout"
    SubcomposeLayout(modifier) { constraints: Constraints ->
        var bottomHeight = 0
        val bottomPlaceables = subcompose("bottom", bottom).map {
            val placeable = it.measure(constraints.copy(minWidth = 0, minHeight = 0))
            bottomHeight = placeable.height
            placeable
        }
        val h = constraints.maxHeight - bottomHeight //        Log.d(TAG, "SubcomposeBottomFirstLayout: max:${constraints.maxHeight} bh:$bottomHeight")
        val otherPlaceables = subcompose("other", other).map {
            it.measure(constraints.copy(minHeight = 0, maxHeight = h))
        }

        layout(constraints.maxWidth, constraints.maxHeight) {
            bottomPlaceables[0].placeRelative(0, h)
            otherPlaceables[0].placeRelative(0, 0)
        }
    }
}