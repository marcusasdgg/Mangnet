package com.example.poomagnet.ui.SettingsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(){
    TopAppBar(title = {Text("Settings")})
}

@Composable
fun SettingsScreen(modifier: Modifier = Modifier){
    val scrollState = rememberScrollState()
    Column(modifier = modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text("About:")
        Spacer(Modifier.height(20.dp))
        Text("Version: beta 0.5")
        Spacer(Modifier.height(20.dp))
        Text("About me: ")
        Text("Hi users of the app, My name is Marcus and I am a 2nd year computer science student in Australia. ")
        Spacer(Modifier.height(20.dp))
        Text("The current controls are very limited due this project being in beta but if you click on the search button it pulls up a filtering system," +
                "to go to the next page scroll naturally or press the left side of the screen and conversely the same for scrolling to the previous page. to download chapters press the download button")
        Spacer(Modifier.height(20.dp))
        Text("About this Project:")
        Text("This project all started at the start of 2024 when a lot of you might remember Tachiyomi going down completely, with the source code etc," +
                "Back then in my first year I thought about starting doing my own Coding projects to have something to put in my resume. I really did not " +
                "like the idea of doing youtube boilerplate projects like everyone else, so I went to thinking about a project to create something unique and most importantly" +
                "something I myself would use.")
        Spacer(Modifier.height(20.dp))
        Text(" Coincidently with the disappearance of Tachiyomi I had the idea of making a manga reading project. Obviously as a First year with only knowledge in C" +
                "I did not have any experience, so I had to first look at the different frameworks to create Apps, I came across many like flutter, swift etc but I mainly went to" +
                "use Jetpack Compose as it was something with plenty of resources to use.)")
        Spacer(Modifier.height(20.dp))
        Text("I learnt so many things whilst doing this project, so much more than even I have learnt at University over the while, it is true that University Coding doesn't actually" +
                "teach you how to code but instead teaches you computer theory. I learnt many things about Ui design, State management, Dependencies and also about the Internet in general")

        Spacer(Modifier.height(20.dp))
        Text("I would like to mention and thank Mangadex for the free usage of their Api and coming with it their huge selection of Manga, Tachiyomi for existing at some point, Google's compose courses which helped me learn" +
                "so much about UI design and app design and All of you for even looking at my App.")
        Spacer(Modifier.height(20.dp))
        Text("There are various things I would love to complete in the future, as you might be aware that this app is currently in its early stages of beta development. That is being" +
                "increasing the overall stability of this app, notifications when the manga is updated and the support of different sources to search manga for. as well as customization options")
    }
}