package net.mononz.joker.lib;

import java.util.Random;

public class JokeSource {

    public static String getJoke() {
        String[] crazyJokes = christmasCrackerJokes;
        int randomIndex = randInt(0, crazyJokes.length-1);
        return crazyJokes[randomIndex];
    }

    private static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    // got some silly jokes from http://www.ducksters.com/jokes/silly.php
    private static final String[] christmasCrackerJokes = new String[]{
            "Q: What goes up and down but does not move ? \nA: Stairs",
            "Q: Where should a 500 pound alien go? \nA: On a diet",
            "Q: What did one toilet say to the other? \nA: You look a bit flushed.",
            "Q: Why did the picture go to jail? \nA: Because it was framed.",
            "Q: What did one wall say to the other wall? \nA: I'll meet you at the corner. ",
            "Q: What did the paper say to the pencil? \nA: Write on!",
            "Q: What do you call a boy named Lee that no one talks to? \nA: Lonely",
            "Q: What gets wetter the more it dries? \nA: A towel.",
            "Q: Why do bicycles fall over? \nA: Because they are two-tired!",
            "Q: Why do dragons sleep during the day? \nA: So they can fight knights!",
            "Q: What did Cinderella say when her photos did not show up? \nA: Someday my prints will come!",
            "Q: Why was the broom late? \nA: It over swept!",
            "Q: What part of the car is the laziest? \nA: The wheels, because they are always tired!",
            "Q: What did the stamp say to the envelope? \nA: Stick with me and we will go places!",
            "Q: What is blue and goes ding dong? \nA: An Avon lady at the North Pole!",
            "Q: We're you long in the hospital? \nA: No, I was the same size I am now!",
            "Q: Why couldn't the pirate play cards? \nA: Because he was sitting on the deck!",
            "Q: What did the laundryman say to the impatient customer? \nA: Keep your shirt on!",
            "Q: What's the difference between a TV and a newspaper? \nA: Ever tried swatting a fly with a TV?",
            "Q: What did one elevator say to the other elevator? \nA: I think I'm coming down with something! ",
            "Q: Why was the belt arrested? \nA: Because it held up some pants!",
            "Q: Why was everyone so tired on April 1st? \nA: They had just finished a March of 31 days.",
            "Q: Which hand is it better to write with? \nA: Neither, it's best to write with a pen! ",
            "Q: Why can't your nose be 12 inches long? \nA: Because then it would be a foot!",
            "Q: What makes the calendar seem so popular? \nA: Because it has a lot of dates!",
            "Q: Why did Mickey Mouse take a trip into space? \nA: He wanted to find Pluto!",
            "Q: What is green and has yellow wheels? \nA: Grass... I lied about the wheels!",
            "Q: What is it that even the most careful person overlooks? \nA: Her nose!",
            "Q: Did you hear about the robbery last night? \nA: Two clothes pins held up a pair of pants!",
            "Q: Why do you go to bed every night? \nA: Because the bed won't come to you! ",
            "Q: Why did Billy go out with a prune? \nA: Because he couldn't find a date! ",
            "Q: Why do eskimos do their laundry in Tide? \nA: Because it's too cold out-tide! ",
            "Q: How do you cure a headache? \nA: Put your head through a window and the pane will just disappear!",
            "Q: What has four wheels and flies? \nA: A garbage truck!",
            "Q: What kind of car does Mickey Mouse's wife drive? \nA: A minnie van!",
            "Q: Why don't traffic lights ever go swimming? \nA: Because they take too long to change!",
            "Q: Why did the man run around his bed? \nA: To catch up on his sleep!",
            "Q: Why did the robber take a bath before he stole from the bank? \nA: He wanted to make a clean get away!"
    };

}