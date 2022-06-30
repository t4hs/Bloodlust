import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 
import processing.net.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Bloodlust extends PApplet {

//import library required to edit code



//declaration of all objects
Menu menu;
Server server;
Server multiplayerServer;
Sound soundSystem;
Count count;
Player player;
KeyPresses keys;
Background bg;
Kill kill;
Spawner spawner;
Endscreen end;
Tutorial tutorial;
Multiplayer mult;

//performed once at the start of the program. Includes all processes that only need to be performed once
public void setup() {
  
  frameRate(60);
  menu = new Menu();
  server = new Server(Bloodlust.this, 1024);
  multiplayerServer= new Server(Bloodlust.this, 1025);
}

//constant loop until the program ends
public void draw() {
  menu.navigate();
}

//registers when a key is pressed down only
public void keyPressed() {
  try{
    keys.keysArray[key] = true;
  } catch(Exception e){
  }
  //takes players name
  if(menu.takingName){
    if (key==ENTER||key==RETURN) {
      String newName[] = new String[1];
      newName[0] = menu.name;
      saveStrings("data/name.txt", newName);
      menu.bgSong.stop();
      if(menu.single){
        end = new Endscreen();
        end.singlePlayerRestart();
        menu.takingName = false;
        menu.singlePlayer = true;
      }
      else{
        String path = dataPath("");
        launch(path+"/addIP/dist/addIP.exe");
        Boolean recieved = false;
        while(recieved == false){
          Client client = server.available();
          if(client != null)
            recieved = true;
          server.write(multiplayerServer.ip());
        }
        mult = new Multiplayer();
        menu.takingName = false;
        menu.connecting = true;
      }
    }
    else if(key==BACKSPACE && menu.name.length()!=0)
      menu.name = menu.name.substring( 0, menu.name.length()-1 );
    else if(key!=CODED && key!=BACKSPACE && key != 32 && menu.name.length() < 20)
      menu.name = menu.name + key;
  }
}

//registers when a key is released only
public void keyReleased() {
  try{
    keys.keysArray[key] = false;
  } catch(Exception e){
  }
}
public void mouseClicked(){
  menu.clicked = true;
}
class Background {
  //methods
  int bgi;
  PImage bg;
  PImage fg;
  float fgx;
  float bgx;
  float bgDelta;

  //constructor
  Background() {
    fill(160, 160, 160);
    noStroke();
    bgi = PApplet.parseInt(random(8));
    bg = loadImage("bg"+bgi+".png");
    fg = loadImage("foreground.png");
    bgx = 0;
    fgx = 0;
    bgDelta = 0.5f;
  }

  //methods
  // used to draw over everything on the screen in the new frame
  public void drawBackground() {
    fill(160, 160, 160);
    image(bg, 0 - bgx, 0);
    image(bg, 1530 - bgx, 0);
    image(fg, 0 - fgx, 335);
    image(fg, 1495 - fgx, 335);
    rect(0, 370, 1530, 20);
    //shift background and foreground to the left
    bgx += bgDelta;
    fgx += bgDelta*20;
    //return the background to its original positoin
    if (bgx >= 1530)
      bgx = 0;
    //return the foreground to its original positoin
    if (fgx >= 1500)
      fgx = 0;
  }
}
class Count {
  //members
  int score;
  int lives;
  int killCount;
  PFont font; 
  
  //constructor
  Count() {
    score = 0;
    killCount = 0;
    lives = 3;
    fill(0);
    font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(30);
    textAlign(LEFT);
  }
  
  //methods
  public void drawCount() {
    fill(0);
    //output all players current match statistics
    text(menu.text[0] + score, 20, 25);
    text(menu.text[1] + killCount, 715, 25);
    text(menu.text[2] + lives, 1400, 25);
    //play charecters quote
    if(score == 5000)
      soundSystem.playQuote();
  }
}
class Endscreen {
  //Memebers
  int playAgainX;
  int playAgainY;
  int playAgainWidth;
  int playAgainHeight;
  int exitX;
  int exitY;
  int exitWidth;
  int exitHeight;
  Boolean saved;
  //constructor
  Endscreen(){
    playAgainX = 400;
    playAgainY = 280;
    playAgainWidth = 110;
    playAgainHeight = 30;
    exitX = 850;
    exitY = 280;
    exitWidth = 45;
    exitHeight = 30;
    }
  
  //methods
  //end the game
  public void gameOver(){
    soundSystem.end();
    background(0);
    fill(255, 0, 0);
    server.write(menu.name+ " " +count.score*count.killCount);
    //recieve data from client
    Client client = server.available();
    if(client!=null){
      String confirmation = client.readString();
      if(confirmation != null)
        server.stop();
    }
    // add to database (through python)
    if(saved == false){
      String path = dataPath("");
      launch(path+"/sendLeaderboard/dist/leaderboard.exe");
      saved = true;
    }
    //display charecters final game statistics
    image(loadImage("gameOver.png"), 200, 30);
    text(menu.text[0] + count.score, 200, 250);
    text(menu.text[1] + count.killCount, 650, 250);
    text(menu.text[3] + (count.score*count.killCount), 1050, 250);
    text(menu.text[4],  400, 300);
    text(menu.text[5],  850, 300);
    //restart game if thew text play again is clicked or exit the program if exit is clicked
    if (mouseX > playAgainX && mouseX < playAgainX + playAgainWidth && mouseY > playAgainY && mouseY < playAgainY + playAgainHeight && mousePressed == true)
      singlePlayerRestart();
    if (mouseX > exitX && mouseX < exitX + exitWidth && mouseY > exitY && mouseY < exitY + exitHeight && mousePressed == true){
      menu.singlePlayer = false;
      menu.bgSong.loop();
    }
  }
  
  public void singlePlayerRestart(){
    //all initial values required to start the single player program
    soundSystem = new Sound();  
    soundSystem.backgroundSong();
    bg = new Background();
    count = new Count();
    keys = new KeyPresses();
    player = new Player();
    spawner = new Spawner();
    kill = new Kill();
    soundSystem.begin();
    soundSystem.endPlayed = false;
    saved = false;
  }
  public void singlePlayer(){
    //all fiunctions for single player gameplay being called
    if(count.lives <= 0){
      soundSystem.endSong();
      end.gameOver();
    }
    else{
      bg.drawBackground();
      player.drawPlayer();
      spawner.spawn();
      spawner.drawEnemies();
      keys.moves();
      spawner.despawn();
      kill.announceKill();
      count.drawCount();
      count.score += 1;
    }
  }
}
class Enemy {
  //members
  int type;
  float x, y;
  float villanFrame;
  PImage sprites;
  PImage villan[];
  ArrayList<Fireball> fireballs = new ArrayList<Fireball>();
  Boolean fireballThrown = false;
  Boolean collided;
  Fireball a;
  
  //methods
  public void drawEnemy() {
    //shift enemy to the left
    x -= bg.bgDelta * 20;
    //draw rocks
    if(type == 0){
      //shift rocks around into the perfect position and then draw them to the screen
      if(villanFrame == 0)
        y = 330;
      else if(villanFrame == 1)
        y = 325;
      else if(villanFrame == 2)
        y = 310;
      else if(villanFrame == 3)
        y = 325;
      image(villan[PApplet.parseInt(villanFrame)], x, y);
    }
    //draw melee villans and have them attack the player
    else if(type == 1){
      if(x <= 320)
      //run through images
        villanFrame = (villanFrame + 0.5f) % 13;
      image(villan[PApplet.parseInt(villanFrame)], x, y);
    }
    //draw ranged enemies
    else if(type == 2){
      if(x > 700)
        image(villan[10], x, y + 10);
      //attack player
      else{
          //run through images
          villanFrame = (villanFrame + 0.3f) % 10;
        image(villan[PApplet.parseInt(villanFrame)], x, y);
        //throw fireballs when animation is at the right point
        if((PApplet.parseInt(villanFrame) == 4 || PApplet.parseInt(villanFrame) == 9) & fireballThrown == false){
          //make sure only one fireball is thrown when at right point of the animation
          fireballThrown = true;
          fireballs.add(new Fireball(x));
        }
        
        if(PApplet.parseInt(villanFrame) != 4 & PApplet.parseInt(villanFrame) != 9){
          //enable the enemy to throw fireballs again
          fireballThrown = false;
        }
        //draw fireballs
        for(int i = 0; i < fireballs.size(); i++){
          //run through entire array of fireballs
          a = fireballs.get(i);
          a.drawFireball();
          //get rid of fireballs bby having player block them or have them hit the enemy.
          if(a.fireballX >= 150 & a.fireballX <= 225 & keys.blocking == true)
            fireballs.remove(i);
          else if(a.fireballX < 100){
            fireballs.remove(i);
            count.lives -= 1;
          }
        }
      }
    }
  }
}
class Fireball{
  //members
  float fireballX;
  float fireballY;
  float fireballXDelta;
  float fireballYDelta;
  float g;
  PImage fireballSprites;
  PImage fireballAnimation[];
  float fireballFrame;
  
  //constructor
  Fireball(float x) {
    fireballX = x - 30;
    fireballY = 300;
    fireballXDelta = 20;
    fireballYDelta = 2;
    g = 2;
    spriteSetup();
  }
  
  //methods
  public void spriteSetup() {
    fireballSprites = loadImage("fireballs.png");
    fireballAnimation = new PImage[6];
    fireballAnimation[0] = fireballSprites.get(0, 0, 85, 84);
    fireballAnimation[1] = fireballSprites.get(85, 0, 85, 84);
    fireballAnimation[2] = fireballSprites.get(170, 0, 85, 84);
    fireballAnimation[3] = fireballSprites.get(255, 0, 85, 84);
    fireballAnimation[4] = fireballSprites.get(340, 0, 85, 84);
    fireballAnimation[5] = fireballSprites.get(425, 0, 85, 84);
  }
  
  public void drawFireball() {
    //run through images
    fireballFrame = (fireballFrame + 0.3f) % 6;
    image(fireballAnimation[PApplet.parseInt(fireballFrame)], fireballX, fireballY);
    //move fireball according to gravity and horizontal velocity
    fireballX -= fireballXDelta;
    fireballY += fireballYDelta;
    fireballYDelta += g;
    //make the fireball bounce off of the ground
    if(fireballY >= 324){
      fireballYDelta *= -1;
      fireballYDelta += g;
    }
  }
}
class KeyPresses {
  //Members
  boolean [] keysArray;
  boolean jumping;
  boolean attacking;
  boolean blocking;
  boolean specialMoving;
  boolean attackFinished;
  char jump;
  char attack;
  char block;
  char specialMove;
  //constructor
  KeyPresses() {
    keysArray = new boolean[128];
    jump = menu.jump;
    attack = menu.attack;
    block = menu.block;
    specialMove = 's';
    moves();
  }
  //Methods
  public void moves() {
    //check input and display the according output
    //make player jump
    if (keysArray[jump] == true && player.y == player.yMin)
      jumping = true;
    //make player attack
    if (keysArray[attack] == true && attackFinished == false)
      attacking = true;
    //make player block
    if (keysArray[block] == true)
      blocking = true;
    //make player perform special move
    if (keysArray[specialMove] == true)
      specialMoving = true;
    //checking if keys are released
    if (keysArray[jump] == false)
      jumping = false;
    if (keysArray[attack] == false)
      attackFinished = false;
    if (keysArray[block] == false){
      blocking = false;
    }
    if (keysArray[specialMove] == false)
      specialMoving = false;
    if(menu.settings){
      jump = menu.jump;
      attack = menu.attack;
      block = menu.block;
    }
  }
}
class Kill {
  //members
  PImage killAnnounce[];
  int announcement;
  int timer;
  Boolean killed;
  //constructor
  Kill(){
    killAnnounce = new PImage[12];
    announcement = 0;
    killed = false;
    for(int i = 0; i < 12; i++)
       killAnnounce[i] = loadImage("killAnnounce" + (i+1) + ".png");
  }
  //Methods
  //draw the correct kill streak image to the screen for 1 second
  public void announceKill(){
    //display image
    if(killed == true)
      image(killAnnounce[announcement], (width / 2) - (killAnnounce[announcement].width / 2), 10);
      //count 1 second
    if(millis() - timer > 1000)
      killed = false;
  }
}
class MeleeVillan extends Enemy{
  //constructor
  MeleeVillan(){
    type = 1;
    x = 1540;
    y = 260;
    spriteSetup();
  }
  
  //methods
  public void spriteSetup() {
    sprites = loadImage("meleeVillan.png");
    villan = new PImage[13];
    villan[0] = sprites.get(1017, 0, 60, 132);
    villan[1] = sprites.get(947, 0, 70, 132);
    villan[2] = sprites.get(873, 0, 74, 132);
    villan[3] = sprites.get(786, 0, 87, 132);
    villan[4] = sprites.get(703, 0, 83, 132);
    villan[5] = sprites.get(617, 0, 86, 132);
    villan[6] = sprites.get(539, 0, 78, 132);
    villan[7] = sprites.get(447, 0, 92, 132);
    villan[8] = sprites.get(355, 0, 92, 132);
    villan[9] = sprites.get(262, 0, 93, 132);
    villan[10] = sprites.get(168, 0, 94, 132);
    villan[11] = sprites.get(82, 0, 86, 132);
    villan[12] = sprites.get(0, 0, 82, 132);
  }
}
class Menu {
  SoundFile bgSong;
  PImage bg;
  int buttonY;
  int playX;
  int characterX;
  int leaderboardX;
  int settingsX;
  int buttonX1;
  int buttonX2;
  int buttonX3;
  int buttonW;
  int buttonH;
  PImage sasuke;
  PImage mario;
  PImage asuna;
  PImage ryu;
  boolean clicked;
  boolean main;
  boolean play;
  boolean character;
  boolean settings;
  boolean leaderboard;
  boolean language;
  boolean control;
  boolean sound;
  boolean singlePlayer;
  boolean startTutorial;
  //smoke
  PImage buffer1;
  PImage buffer2;
  PImage cooling;
  float yInitial = 0.0f;
  //language settings
  String text[];
  //control settings
  char block;
  char attack;
  char jump;
  boolean selectBlock;
  boolean selectAttack;
  boolean selectJump;
  //sound settings
  boolean gameplay;
  boolean music;
  //character select
  String characterPath;
  //Taking player name
  Boolean takingName;
  String name;
  //leaderboard
  int c1;
  int c2;
  int barX;
  int barW;
  PImage first;
  PImage second;
  PImage third;
  Boolean sent;
  Boolean received;
  String values[];
  //multiplpayer game mode
  Boolean single;
  Boolean multiplayer;
  Boolean connecting;
  Menu() {
    bgSong = new SoundFile(Bloodlust.this, "menuSong.wav");
    bg = loadImage("bg.png");
    buttonY = 350;
    playX = (width/5) - 100;
    characterX = (width/5)*2 - 100;
    leaderboardX = (width/5)*3 - 100;
    settingsX = (width/5)*4 - 100;
    buttonX1 = (width/4) - 100;
    buttonX2 = (width/4)*2 - 100;
    buttonX3 = (width/4)*3 - 100;
    buttonW = 200;
    buttonH = 50;
    c1 = 0xffC0C0C0;
    c2 = 0xffD3D3D3;
    barX = (width/2)+50;
    barW = 400;
    values = new String[20];
    first = loadImage("1st.png");
    second = loadImage("2nd.png");
    third = loadImage("3rd.png");
    sasuke = loadImage("sasuke.png");
    mario = loadImage("mario.png");
    asuna = loadImage("asuna.png");
    ryu = loadImage("ryu.png");
    main = true;
    //smoke
    buffer1 = createImage(width, 130, RGB);
    buffer2 = createImage(width, 130, RGB);
    cooling = createImage(width, 130, RGB);
    text = loadStrings(join(loadStrings("language.txt"), ""));
    block = join(loadStrings("block.txt"), "").charAt(0);
    attack = join(loadStrings("attack.txt"), "").charAt(0);
    jump = join(loadStrings("jump.txt"), "").charAt(0);
    if(join(loadStrings("gameplay.txt"), "").charAt(0) == 'y')
      gameplay = true;
    else
      gameplay = false;
    if(join(loadStrings("music.txt"), "").charAt(0) == 'y'){
      music = true;
      bgSong.loop();
    }
    else
      music = false;
    takingName = false;
    multiplayer = false;
    connecting = false;
  }
  
  public void mainMenu(){
    //display main menu
    received = false;
    sent = false;
    image(bg, 0,0);
    image(loadImage("logo.png"), 400, 30);
    button(playX, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[6]);
    button(characterX, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[7]);
    button(leaderboardX, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[8]);
    button(settingsX, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[9]);
    //colision detection
    if((mouseX > playX && mouseX < playX + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      play = true;
      main = false;
    }
    else if ((mouseX > characterX && mouseX < characterX + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      character = true;
      main = false;
    }
    else if ((mouseX > leaderboardX && mouseX < leaderboardX + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      leaderboard = true;
      main = false;
    }
    else if ((mouseX > settingsX && mouseX < settingsX + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      settings = true;
      main = false;
    }
  }
  public void playMenu(){
    //display play menu
    image(bg, 0,0);
    image(loadImage("logo.png"), 400, 30);
    button(buttonX1, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[10]);
    button(buttonX2, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[11]);
    button(buttonX3, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[12]);
    drawArrow(1330,365);
    if((mouseX > 1330 && mouseX < 1330 + 40 )&&(mouseY > 365 - 10 && mouseY < 365 + 30) && clicked){
      play = false;
      main = true;
    }
    else if((mouseX > buttonX1 && mouseX < buttonX1 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      name = join(loadStrings("name.txt"), "");
      takingName = true;
      single = true;
    }
    else if ((mouseX > buttonX2 && mouseX < buttonX2 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      name = join(loadStrings("name.txt"), "");
      takingName = true;
      single = false;
    }
    else if((mouseX > buttonX3 && mouseX < buttonX3 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      bgSong.stop();
      tutorial = new Tutorial();
      tutorial.tutorialRestart();
      startTutorial = true;
    }
  }
  
  public void leaderboard(){
    if(received == false){
      if(sent == false){
        String path = dataPath("");
        launch(path+"/receiveLeaderboard/dist/topPlayers.exe");
        sent = true;
      }
      Client client = server.available();
      if(client!=null){
        String listOfValues = client.readString();
        if(listOfValues != null){
          values = split(listOfValues, " ");
          server.stop();
          received = true;
        }
      }
    }
    else{
      image(bg, 0,0);
      image(loadImage("logo.png"), 50, 50);
      noStroke();
      textAlign(CENTER);
      textSize(30);
      fill(0);
      rect(barX, 42, barW,42);
      fill(255);
      text("Name", barX+(barW/4), 75);
      text("Score", barX+(barW/4*3), 75);
      fill(c1);
      rect(barX, 84, barW,41);
      fill(c2);
      rect(barX, 125, barW,41);
      fill(c1);
      rect(barX, 166, barW,41);
      fill(c2);
      rect(barX, 207, barW,41);
      fill(c1);
      rect(barX, 248, barW,41);
      fill(c2);
      rect(barX, 289, barW,41);
      fill(c1);
      rect(barX, 330, barW,41);
      fill(c2);
      rect(barX, 371, barW,41);
      fill(c1);
      rect(barX, 412, barW,41);
      fill(c2);
      rect(barX, 453, barW,41);
      image(first, barX-46, 84);
      image(second, barX-46, 125);
      image(third, barX-43, 166);
      text("4th", barX-23, 236);
      text("5th", barX-23, 282);
      text("6th", barX-23, 320);
      text("7th", barX-23, 360);
      text("8th", barX-23, 400);
      text("9th", barX-23, 440);
      text("10th", barX-23, 482);
      fill(0);
      text(values[0], barX+(barW/4), 116);
      text(values[1], barX+(barW/4), 157);
      text(values[2], barX+(barW/4), 198);
      text(values[3], barX+(barW/4), 239);
      text(values[4], barX+(barW/4), 280);
      text(values[5], barX+(barW/4), 321);
      text(values[6], barX+(barW/4), 362);
      text(values[7], barX+(barW/4), 403);
      text(values[8], barX+(barW/4), 444);
      text(values[9], barX+(barW/4), 485);
      text(values[10], barX+(barW/4*3), 116);
      text(values[11], barX+(barW/4*3), 157);
      text(values[12], barX+(barW/4*3), 198);
      text(values[13], barX+(barW/4*3), 239);
      text(values[14], barX+(barW/4*3), 280);
      text(values[15], barX+(barW/4*3), 321);
      text(values[16], barX+(barW/4*3), 362);
      text(values[17], barX+(barW/4*3), 403);
      text(values[18], barX+(barW/4*3), 444);
      text(values[19], barX+(barW/4*3), 485);
      drawArrow(1350,450);
      if((mouseX > 1350 && mouseX < 1350 + 40 )&&(mouseY > 450 - 10 && mouseY < 450 + 30) && clicked){
        leaderboard = false;
        main = true;
      }
    }
  }
  public void selectCharacter(){
    image(bg, 0,0);
    //creating character select cards i reused some coordinates from main menu
    button(playX, 90, buttonW, 400, 0,   0xffFFD700, "");
    image(sasuke, playX, 90);
    button(characterX, 90, buttonW, 400, 0,   0xffFFD700, "");
    image(mario, characterX - 110, 110);
    button(leaderboardX, 90, buttonW, 400, 0,   0xffFFD700, "");
    image(asuna, leaderboardX- 30, 90);
    button(settingsX, 90, buttonW, 400, 0,   0xffFFD700, "");
    image(ryu, settingsX-30, 90);
    //text for the top of the screen (character names)
    strokeWeight(1);
    textAlign(CENTER);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(90);
    fill(0xffFFD700);
    //changing text
    String select;
    if((mouseX > playX && mouseX < playX + buttonW )&&(mouseY > 90 && mouseY < 490))
      select = "Sazuke";
    else if((mouseX > characterX && mouseX < characterX + buttonW )&&(mouseY > 90 && mouseY < 490))
      select = "Maryo";
    else if((mouseX > leaderboardX && mouseX < leaderboardX + buttonW )&&(mouseY > 90 && mouseY < 490))
      select = "Asuma";
    else if((mouseX > settingsX && mouseX < settingsX + buttonW )&&(mouseY > 90 && mouseY < 490))
      select = "Riu";
    else
      select = text[33];
    text(select, width/2 , 70);
    drawArrow(1350,450);
    if((mouseX > 1350 && mouseX < 1350 + 40 )&&(mouseY > 450 - 10 && mouseY < 450 + 30) && clicked){
      character = false;
      main = true;
    }
    if((mouseX > playX && mouseX < playX + buttonW )&&(mouseY > 90 && mouseY < 490) && clicked){
      String newCharacter[] = new String[1];
      newCharacter[0] = "sasukeSprites/";
      saveStrings("data/character.txt", newCharacter);
      character = false;
      main = true;
    }
    else if((mouseX > characterX && mouseX < characterX + buttonW )&&(mouseY > 90 && mouseY < 490) && clicked){
      String newCharacter[] = new String[1];
      newCharacter[0] = "marioSprites/";
      saveStrings("data/character.txt", newCharacter);
      character = false;
      main = true;
    }
    else if((mouseX > leaderboardX && mouseX < leaderboardX + buttonW )&&(mouseY > 90 && mouseY < 490) && clicked){
      String newCharacter[] = new String[1];
      newCharacter[0] = "asunaSprites/";
      saveStrings("data/character.txt", newCharacter);
      character = false;
      main = true;
    }
    else if((mouseX > settingsX && mouseX < settingsX + buttonW )&&(mouseY > 90 && mouseY < 490) && clicked){
      String newCharacter[] = new String[1];
      newCharacter[0] = "ryuSprites/";
      saveStrings("data/character.txt", newCharacter);
      character = false;
      main = true;
    }
      
  }
  
  public void settingsMenu(){
    //display play menu
    image(bg, 0,0);
    image(loadImage("logo.png"), 400, 30);
    button(buttonX1, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[13]);
    button(buttonX2, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[14]);
    button(buttonX3, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[15]);
    drawArrow(1330,365);
    if((mouseX > buttonX1 && mouseX < buttonX1 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      language = true;
      settings = false;
    }
    else if ((mouseX > buttonX2 && mouseX < buttonX2 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      control = true;
      settings = false;
    }
    else if ((mouseX > buttonX3 && mouseX < buttonX3 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      sound = true;
      settings = false;
    }
    if((mouseX > 1330 && mouseX < 1330 + 40 )&&(mouseY > 365 - 10 && mouseY < 365 + 30) && clicked){
      settings = false;
      main = true;
    }
  }
  public void languageMenu(){
    //display play menu
    image(bg, 0,0);
    image(loadImage("logo.png"), 400, 30);
    button(buttonX1, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, "Deutsche");
    button(buttonX2, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, "English");
    button(buttonX3, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, "Française");
    drawArrow(1330,365);
    if((mouseX > 1330 && mouseX < 1330 + 40 )&&(mouseY > 365 - 10 && mouseY < 365 + 30) && clicked){
      language = false;
      settings = true;
    }
    else if((mouseX > buttonX1 && mouseX < buttonX1 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      text = loadStrings("german.txt");
      String newLanguage[] = new String[1];
      newLanguage[0] = "german.txt";
      saveStrings("data/language.txt", newLanguage);
      language = false;
      main = true;
    }
    else if((mouseX > buttonX2 && mouseX < buttonX2 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      text = loadStrings("english.txt");
      String newLanguage[] = new String[1];
      newLanguage[0] = "english.txt";
      saveStrings("data/language.txt", newLanguage);
      language = false;
      main = true;
    }
    else if((mouseX > buttonX3 && mouseX < buttonX3 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      text = loadStrings("french.txt");
      String newLanguage[] = new String[1];
      newLanguage[0] = "french.txt";
      saveStrings("data/language.txt", newLanguage);
      language = false;
      main = true;
    }
  }
  public void controlMenu(){
    //display play menu
    image(bg, 0,0);
    image(loadImage("logo.png"), 400, 30);
    button(buttonX1, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[16]);
    button(buttonX2, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[17]);
    button(buttonX3, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[18]);
    drawArrow(1330,365);
    if((mouseX > 1330 && mouseX < 1330 + 40 )&&(mouseY > 365 - 10 && mouseY < 365 + 30) && clicked){
      control = false;
      settings = true;
    }
    else if((mouseX > buttonX1 && mouseX < buttonX1 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      control = false;
      selectBlock = true;
    }
    else if((mouseX > buttonX2 && mouseX < buttonX2 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      control = false;
      selectAttack = true;
    }
    else if((mouseX > buttonX3 && mouseX < buttonX3 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      control = false;
      selectJump = true;
    }
  }
  public void selectBlock(){
    background(0);
    fill(255);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(90);
    text("Press any key", width/2, height/2);
    if(keyPressed){
      String newKey[] = new String[1];
      newKey[0] = str(key);
      saveStrings("data/block.txt", newKey);
      selectBlock = false;
      control = true;
      block = key;
    }
  }
  public void selectAttack(){
    background(0);
    fill(255);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(90);
    text("Press any key", width/2, height/2);
    if(keyPressed){
      String newKey[] = new String[1];
      newKey[0] = str(key);
      saveStrings("data/attack.txt", newKey);
      selectAttack = false;
      control = true;
      attack = key;
    }
  }
  public void selectJump(){
    background(0);
    fill(255);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(90);
    text("Press any key", width/2, height/2);
    if(keyPressed){
      String newKey[] = new String[1];
      newKey[0] = str(key);
      saveStrings("data/jump.txt", newKey);
      selectJump = false;
      control = true;
      jump = key;
    }
  }
  public void soundMenu(){
    //display play menu
    image(bg, 0,0);
    image(loadImage("logo.png"), 400, 30);
    button(buttonX1, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[19]);
    button(buttonX2, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[20]);
    button(buttonX3, buttonY, buttonW, buttonH, 0xffFF0000,   0xffFFD700, text[21]);
    drawArrow(1330,365);
    if((mouseX > 1330 && mouseX < 1330 + 40 )&&(mouseY > 365 - 10 && mouseY < 365 + 30) && clicked){
      sound = false;
      settings = true;
    }
    else if((mouseX > buttonX1 && mouseX < buttonX1 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      if(gameplay){
        gameplay = false;
        String soundSetting[] = new String[1];
        soundSetting[0] = "n";
        saveStrings("data/gameplay.txt", soundSetting);
      }
      else{
        gameplay = true;
        String soundSetting[] = new String[1];
        soundSetting[0] = "y";
        saveStrings("data/gameplay.txt", soundSetting);
      }
    }
    else if((mouseX > buttonX2 && mouseX < buttonX2 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      if(music){
        music = false;
        bgSong.stop();
        String soundSetting[] = new String[1];
        soundSetting[0] = "n";
        saveStrings("data/music.txt", soundSetting);
    }
      else{
        music = true;
        bgSong.loop();
        String soundSetting[] = new String[1];
        soundSetting[0] = "y";
        saveStrings("data/music.txt", soundSetting);
      }
    }
    else if((mouseX > buttonX3 && mouseX < buttonX3 + buttonW )&&(mouseY > buttonY && mouseY < buttonY + buttonH) && clicked){
      //toggles both according to gameplays state
      if(gameplay){
        gameplay = false;
        music = false;
        bgSong.stop();
        String soundSetting[] = new String[1];
        soundSetting[0] = "n";
        saveStrings("data/gameplay.txt", soundSetting);
        saveStrings("data/music.txt", soundSetting);
      }
      else{
        gameplay = true;
        music = true;
        bgSong.loop();
        String soundSetting[] = new String[1];
        soundSetting[0] = "y";
        saveStrings("data/gameplay.txt", soundSetting);
        saveStrings("data/music.txt", soundSetting);
      }
    }
    if(gameplay == false){
      line(buttonX1, buttonY + buttonH, buttonX1 + buttonW, buttonY);
    }
    if(music == false){
      line(buttonX2, buttonY + buttonH, buttonX2 + buttonW, buttonY);
    }
  }
  public void takeName(){
    background(0);
    fill(255);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(90);
    text ("ENTER YOUR NAME\n"+name, width/2, height/2);
  }
  public void button(float x, float y, float w, float h, int colour, int glow, String caption) {
    //parameters for text
    strokeWeight(1);
    textAlign(CENTER);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(30);
    int text;
    //creating a glowing outline and text
    if((mouseX > x && mouseX < x + w )&&(mouseY > y && mouseY < y + h)){
      stroke(glow);
      text = glow;
    }
    else{
      stroke(0);
      text = 0;
    }
    //creating button
    fill(colour);
    rect(x,y,w,h,7);
    fill(text);
    text(caption, x + (w/2), y + (h/2 + 7));
  }
  public void drawArrow(int x, int y){
    if((mouseX > x && mouseX < x + 40 )&&(mouseY > y - 10 && mouseY < y + 30)){
        stroke(0xffFFD700);
  
      }
    else{
      stroke(0);
    }
    noFill();
    strokeWeight(5);
    arc(x+18, y+10, 40, 40, PI+QUARTER_PI, TWO_PI);
    arc(x+18, y+10, 40, 40, 0, PI);
    pushMatrix();
    translate(x, y);
    strokeWeight(5);
    rotate(radians(130));
    line(0, 0, - 8, -8);
    line(0, 0, - 8, 8);
    popMatrix();
  }
  public void navigate(){
    if(singlePlayer)
      end.singlePlayer();
    else if(connecting)
      mult.connectToPlayers();
    else if(multiplayer)
      mult.multiplayer();
    else if(startTutorial)
      tutorial.tutorial();
    else if(takingName)
      takeName();
    else if(main){
      mainMenu();
    }
    else if(play){
      playMenu();
    }
    else if(leaderboard){
      leaderboard();
    }
    else if(character){
      selectCharacter();
    }
    else if(settings){
      settingsMenu();
    }
    else if(language){
      languageMenu();;
    }
    else if(control){
      delay(100);
      controlMenu();
    }
    else if(sound){
      soundMenu(); 
    }
    else if(selectBlock){
      selectBlock();
    }
    else if(selectAttack){
      selectAttack();
    }
    else if(selectJump){
      selectJump();
    }
    clicked = false;
  }
  //smoke
  public void newLine(int rows) {
    //create row of white pixels
    buffer1.loadPixels();
    for (int x = 0; x<  buffer1.width; x++){
      for(int j = 0; j< rows; j++){
        //find location to draw pixels
        int y = buffer1.height - (j+1); 
        int index = x + y * buffer1.width;
        buffer1.pixels[index] = color(255);
      }
    }
    buffer1.updatePixels();
  }
  public void cool(){
    cooling.loadPixels();
    //start x at 0
    float xoff = 0.0f;
    float increment = 0.02f;
    for (int x = 0; x < cooling.width; x++){
      xoff += increment;
      //start y at 0
      float yoff = yInitial;
      for (int y = 0; y < cooling.height; y++){
        yoff += increment;
        //calculate noise and enlarge
        float n = noise(xoff, yoff);
        if(n<0.4f)
          n = 0;
        float bright = noise(xoff, yoff) *25;
        //set pixel to grayscale value
        cooling.pixels[x+y*cooling.width]= color(bright);
      }
    }
    cooling.updatePixels();
    yInitial += increment;
  }
  public void createSmoke(){
    cool();
    newLine(10);
    buffer1.loadPixels();
    buffer2.loadPixels();
    //look through all x and y coordinates and find the neightboring pixels colour
    for (int x = 1; x < buffer1.width-1; x++) {
      for (int y = 1; y < buffer1.height-1; y++) {
        int index0 = x + y * buffer1.width;
        int index1 = (x+1) + y * buffer1.width;
        int index2 = (x-1) + y * buffer1.width;
        int index3 = x + (y+1) * buffer1.width;
        int index4 = x + (y-1) * buffer1.width;
        int c1 = buffer1.pixels[index1];
        int c2 = buffer1.pixels[index2];
        int c3 = buffer1.pixels[index3];
        int c4 = buffer1.pixels[index4];
        int c5 = cooling.pixels[index0];
        float newC = brightness(c1) + brightness(c2)+ brightness(c3) + brightness(c4);
        newC = newC - brightness(c5);
        newC = newC / 4;
        buffer2.pixels[index4] = color(newC);
        if(color(buffer2.pixels[index0])==color(0));
          buffer2.pixels[index0] = color(255,0);
        if(color(buffer1.pixels[index0])==color(0));
          buffer1.pixels[index0] = color(255,0);
      }
    }
    buffer2.updatePixels();
    
    //swap
    PImage temp = buffer1;
    buffer1 = buffer2;
    buffer2 = temp;
    image(buffer2, 0, height-buffer2.height);
    for (int x = 1; x < buffer1.width-1; x++) {
      for (int y = 1; y < buffer1.height-1; y++) {
        int index0 = x + y * buffer1.width;
        if(color(buffer2.pixels[index0])==color(255,0))
          buffer2.pixels[index0] = color(0);
        if(color(buffer1.pixels[index0])==color(255,0));
          buffer1.pixels[index0] = color(0);
      }
    }
  }
}
class Multiplayer{
   //Memebers
  int playAgainX;
  int playAgainY;
  int playAgainWidth;
  int playAgainHeight;
  int exitX;
  int exitY;
  int exitWidth;
  int exitHeight;
  Boolean saved;
  Boolean connecting;
  String players[][];
  ArrayList<String> dead;
  Boolean rankConfirmed;
  Client multiServerClients[] = new Client[4];
  //constructor
  Multiplayer(){
    playAgainX = 400;
    playAgainY = 280;
    playAgainWidth = 110;
    playAgainHeight = 30;
    exitX = 850;
    exitY = 280;
    exitWidth = 45;
    exitHeight = 30;
    connecting = false;
    players = new String[4][3];
    rankConfirmed = false;
    }
  
  //methods
  //end the game
  public void gameOver(){
    soundSystem.end();
    background(0);
    fill(255, 0, 0);
    //write score to python script to add to leaderboard
    server.write(menu.name+ " " +count.score*count.killCount);
    //recieve data from client
    Client client = server.available();
    if(client!=null){
      String confirmation = client.readString();
      if(confirmation != null)
        server.stop();
    }
    // add to database (through python)
    if(saved == false){
      String path = dataPath("");
      launch(path+"/sendLeaderboard/dist/leaderboard.exe");
      saved = true;
    }
    //display charecters final game statistics
    int rank = 0;
    String subscript;
    if(rankConfirmed == false)
      rank = 4 - dead.size();
    //adding subscript after number
    if(rank == 1)
      subscript = "st";
    else if(rank == 2)
      subscript = "nd";
    else if(rank == 3)
      subscript = "rd";
    else
      subscript = "th";
    textSize(90);
    text(rank + subscript, 200, 30);
    textSize(30);
    text(menu.text[0] + count.score, 200, 250);
    text(menu.text[1] + count.killCount, 650, 250);
    text(menu.text[3] + (count.score*count.killCount), 1050, 250);
    text(menu.text[4],  400, 300);
    text(menu.text[5],  850, 300);
    //allow playerws to exit the game after dying
    if (mouseX > exitX && mouseX < exitX + exitWidth && mouseY > exitY && mouseY < exitY + exitHeight && mousePressed == true){
      menu.multiplayer = false;
      menu.bgSong.loop();
    //draw players scores
    drawAllScores();
    }
  }
  
  public void multiplayerRestart(){
    //all initial values required to start the single player program
    soundSystem = new Sound();  
    soundSystem.backgroundSong();
    bg = new Background();
    count = new Count();
    keys = new KeyPresses();
    player = new Player();
    spawner = new Spawner();
    kill = new Kill();
    soundSystem.begin();
    soundSystem.endPlayed = false;
    saved = false;
  }
  public void multiplayer(){
    //all fiunctions for single player gameplay being called
    if(count.lives <= 0){
      soundSystem.endSong();
      updateServers();
      mult.gameOver();
    }
    else{
      bg.drawBackground();
      player.drawPlayer();
      spawner.spawn();
      spawner.drawEnemies();
      keys.moves();
      spawner.despawn();
      kill.announceKill();
      count.drawCount();
      count.score += 1;
      updateServers();
      drawAllScores();
    }
  }
  public void connectToPlayers(){
    background(0);
    String addresses[] = new String[4];
    //launch connection once
    if(connecting ==false){
      Client client = server.available();
      String connect = client.readString();
      String path = dataPath("");
      launch(path+"/returnIP/dist/returnIP.exe");
      connecting = true;
    }
    Client client = server.available();
    if(client!=null){
      String connect = client.readString();
      if(connect != null){
        if(PApplet.parseInt(connect) == 1 || PApplet.parseInt(connect) == 2 || PApplet.parseInt(connect) == 3 || PApplet.parseInt(connect) == 4){
          fill(255);
          text("waiting for players\n"+connect+"/4",width/2, height/2);
        }
        else{
          //remove square brackets
          connect = connect.substring(1, connect.length()-1);
          // split into array
          addresses = split(connect, ", ");
          for(int i = 0; i < 4; i++){
            multiServerClients[i] = new Client(Bloodlust.this, addresses[i], 1025);
            multiServerClients[i].write(client.ip() + " " + menu.name);
          }
          //do this for all 4 players
          for(int i = 0; i < 4; i++){
            //make sure all players have been added
            while(players[i] == null){
              Client registerPlayers = multiplayerServer.available();
              if(registerPlayers != null){
                String add = registerPlayers.readString();
                String IPAndName[] = new String[2];
                // split name and IP
                IPAndName = split(add, " ");
                //ensure name and IP hasnt already been added
                Boolean added = false;
                for(int j = 0; j <4; j++){
                  if(IPAndName[0] == players[j][0])
                    added = true;
                }
                //add the players
                if(added == false){
                  players[i][0] = IPAndName[0];
                  players[i][1] = IPAndName[1];
                }
              }
            }
          }
          String path = dataPath("");
          launch(path+"/dropIP/dist/dropTableContents.exe");
          menu.connecting = false;
        }
      }
    }
  }
  public void updateServers(){
    for(int i = 0; i < 4; i++){
      multiServerClients[i].write(menu.name + " " + count.score*count.killCount);
    }
    //update four player scores per game loop
    for(int i = 0; i<4; i++){
      //add new player score into an array
      Client updateScore = multiplayerServer.available();
      String newScore[] = new String[2];
      newScore = split(updateScore.readString(), " ");
      //check for identical name
      for(int j = 0; j <4; j++){
        if(players[j][1] == newScore[0]){
          //checking if scores have changed
          if(players[j][2] == newScore[1]){
            //check if already dead
            Boolean alreadyDead = false;
            for(String a: dead){
              //split to find player names
              String b[] = split(a, " ");
              if(b[0] == players[j][1])
                alreadyDead = true;
            }
            //add to dead players if not already in the list
            if(alreadyDead == false)
              dead.add(players[j][1] + " " + players[j][2]);
          }
          //add the score
          players[j][2] = newScore[1];
        }
      }
    }
  }
  public void drawAllScores(){
    fill(255,0,0);
    text(players[0][1] + ": " + players[0][2], (width/5) - 100, 400);
    text(players[1][1] + ": " + players[1][2], (width/5)*2 - 100, 400);
    text(players[2][1] + ": " + players[2][2], (width/5)*3 - 100, 400);
    text(players[3][1] + ": " + players[3][2], (width/5)*4 - 100, 400);
  }
}
class Obstacle extends Enemy{
  //constructor
  Obstacle(){
    type = 0;
    collided = false;
    x = 1540;
    y = 320;
    spriteSetup();
    villanFrame = PApplet.parseInt(random(0,4));
  }
  //methods
  public void spriteSetup() {
    sprites = loadImage("obstacles.png");
    villan = new PImage[4];
    villan[0] = sprites.get(48, 28, 79, 43);
    villan[1] = sprites.get(169, 22, 138, 47);
    villan[2] = sprites.get(22, 80, 153, 63);
    villan[3] = sprites.get(201, 95, 102, 46);
  }
}
class Player {
  //members
  //set the players coordinates
  float x, y;
  float yMin;
  //Game componenets
  int energy;
  //Running Animation
  PImage runSprites;
  PImage runAnimation[];
  float runFrame;
  //Jumping Animation
  PImage jumpSprites;
  PImage jumpAnimation[];
  //Attack Animations
  PImage attackSprites;
  PImage attackAnimation1[];
  PImage attackAnimation2[];
  int animationNumber;
  float attackFrame;
  //Block Animations
  PImage blockSprites1;
  PImage blockSprites2;
  PImage blockAnimation1[];
  PImage blockAnimation2[];
  PImage blockLightning[];
  float blockFrame;
  float blockFrame2;
  //Special move animations
  //PImage specialMoveSprites;
  //PImage specialMoveAnimation[];
  //PImage lightning[];
  //float specialMoveFrame;
  //boolean specialMoveStartPosition;
  //boolean specialMoveCharged;
  //boolean specialMoveFinished;
  //int timer;
  
  //constructor
  Player() {
    yMin = 300;
    x = 100;
    y = yMin;
    runFrame = 0;
    attackFrame = 0;
    blockFrame = 0;
    blockFrame2 = 0;
    //specialMoveFrame = 4;
    energy = 0;
    spriteSetup();
    //specialMoveFinished = true;
  }

  //Methods
  public void spriteSetup() {
    //Running Animation
    runSprites = loadImage(menu.characterPath + "runSprites.png");
    runAnimation = new PImage[6];
    runAnimation[0] = runSprites.get(4, 10, 77, 78);
    runAnimation[1] = runSprites.get(91, 10, 74, 78);
    runAnimation[2] = runSprites.get(166, 10, 66, 78);
    runAnimation[3] = runSprites.get(241, 10, 73, 78);
    runAnimation[4] = runSprites.get(323, 10, 79, 78);
    runAnimation[5] = runSprites.get(423, 10, 73, 78);
    //Jumping Animation
    jumpSprites = loadImage(menu.characterPath + "jumpSprites.png");
    jumpAnimation = new PImage[3];
    jumpAnimation[0] = jumpSprites.get(11, 26, 62, 54);
    jumpAnimation[1] = jumpSprites.get(78, 7, 52, 73);
    jumpAnimation[2] = jumpSprites.get(146, 4, 56, 77);
    //Attack Animation
    attackSprites = loadImage(menu.characterPath + "attackSprites.png");
    attackAnimation1 = new PImage[4];
    attackAnimation2 = new PImage[4];
    attackAnimation1[0] = attackSprites.get(3, 5, 64, 84);
    attackAnimation1[1] = attackSprites.get(87, 7, 89, 81);
    attackAnimation1[2] = attackSprites.get(186, 9, 72, 75);
    attackAnimation1[3] = attackSprites.get(262, 9, 71, 75);
    attackAnimation2[0] = attackSprites.get(6, 140, 63, 92);
    attackAnimation2[1] = attackSprites.get(70, 120, 104, 112);
    attackAnimation2[2] = attackSprites.get(192, 152, 106, 78);
    attackAnimation2[3] = attackSprites.get(299, 174, 106, 55);
    //Blocking Animation
    blockSprites1 = loadImage(menu.characterPath + "blockSprites1.png");
    blockSprites2 = loadImage(menu.characterPath + "blockSprites2.png");
    blockAnimation1 = new PImage[4];
    blockAnimation2 = new PImage[6];
    blockLightning = new PImage[8];
    blockAnimation1[0] = blockSprites1.get(6, 4, 46, 97);
    blockAnimation1[1] = blockSprites1.get(60, 4, 52, 97);
    blockAnimation1[2] = blockSprites1.get(131, 30, 86, 69);
    blockAnimation1[3] = blockSprites1.get(226, 26, 71, 69);
    blockAnimation2[0] = blockSprites2.get(4, 11, 84, 66);
    blockAnimation2[1] = blockSprites2.get(95, 9, 82, 66);
    blockAnimation2[2] = blockSprites2.get(189, 9, 72, 68);
    blockAnimation2[3] = blockSprites2.get(279, 10, 80, 66);
    blockAnimation2[4] = blockSprites2.get(387, 10, 85, 67);
    blockAnimation2[5] = blockSprites2.get(496, 7, 80, 70);
    blockLightning[0] = blockSprites1.get(340, 28, 19, 16);
    blockLightning[1] = blockSprites1.get(398, 28 ,23 ,16);
    blockLightning[2] = blockSprites1.get(333, 114, 33, 67);
    blockLightning[3] = blockSprites1.get(375, 114, 33, 67);
    blockLightning[4] = blockSprites1.get(409, 114, 33, 67);
    blockLightning[5] = blockSprites1.get(451, 114, 33, 67);
    blockLightning[6] = blockSprites1.get(7, 158, 81, 23);
    blockLightning[7] = blockSprites1.get(240, 158, 74, 23);
    ////Special Move Animation
    //specialMoveSprites = loadImage("specialMoveSprites.png");
    //specialMoveAnimation = new PImage[14];
    //lightning = new PImage[4];
    //specialMoveAnimation[0] = specialMoveSprites.get(12, 10, 52, 57);
    //specialMoveAnimation[1] = specialMoveSprites.get(80, 11, 54, 57);
    //specialMoveAnimation[2] = specialMoveSprites.get(146, 9, 54, 61);
    //specialMoveAnimation[3] = specialMoveSprites.get(224, 7, 99, 67);
    //specialMoveAnimation[4] = specialMoveSprites.get(343, 5, 99, 68);
    //specialMoveAnimation[5] = specialMoveSprites.get(446, 11, 108, 60);
    //specialMoveAnimation[6] = specialMoveSprites.get(560, 17, 100, 60);
    //specialMoveAnimation[7] = specialMoveSprites.get(797, 37, 73, 44);
    //specialMoveAnimation[8] = specialMoveSprites.get(891, 42, 71, 38);
    //specialMoveAnimation[9] = specialMoveSprites.get(964, 41, 50, 33);
    //specialMoveAnimation[10] = specialMoveSprites.get(32, 91, 31, 25);
    //specialMoveAnimation[11] = specialMoveSprites.get(72, 92, 56, 48);
    //specialMoveAnimation[12] = specialMoveSprites.get(141, 86, 49, 39);
    //specialMoveAnimation[13] = specialMoveSprites.get(201, 91, 57, 34);
    //lightning[0] = loadImage("lightning.png").get(-50, 0, 124, 255);
    //lightning[1] = loadImage("lightning.png").get(105, 0, 174, 255);
    //lightning[2] = loadImage("lightning.png").get(300, 0, 224, 255);
    //lightning[3] = loadImage("lightning.png").get(510, 0, 147, 255);
  }
  //draw the correct animation based on player input
  public void drawPlayer() {
    //select which sprites to play depending on player input
    //if (keys.specialMoving == true && energy== 100 || specialMoveFinished == false)
    //  specialMoveAnimation();
    if (keys.blocking == true)
      blockAnimation();
    else if (keys.jumping == true || y != yMin)
      jumpAnimation();
    else if (keys.attacking == true)
      attackAnimation();
    else{
      blockFrame = 0;
      runAnimation();
    }
  }
  // cycle through images of the player running
  public void runAnimation() {
    //cycle through images
    runFrame = (runFrame + 0.3f) % 6;
    //adjust players position
    if (runFrame == 0)
      x = 98;
    else if (runFrame == 1)
      x = 101;
    else if (runFrame == 2)
      x = 109;
    else if (runFrame == 3)
      x = 102;
    else if (runFrame == 4)
      x = 96;
    else if (runFrame == 5)
      x = 102;
      //draw image
    image(runAnimation[PApplet.parseInt(runFrame)], x, y);
  }
  //cycle through images of the player jumping
  public void jumpAnimation() {
    float yDelta = 0;
    float ySpeed = 5;
    float yMax = 200;
    //have the player prepare to jump
    if (y == yMin){
      soundSystem.jumpSound();
      image(jumpAnimation[0], x, y);
    }
    //signals the top of the jump
    if (y == yMax)
      keys.jumping = false;
    //make the player rise
    if (keys.jumping == true && y != yMax) {
      yDelta = ySpeed;
      image(jumpAnimation[1], x, y);
    }
    //make the player fall
    if (keys.jumping == false && y != yMin) {
      yDelta = -ySpeed;
      image(jumpAnimation[2], x, y);
    }
    y -= yDelta;
  }
  ////cycle through images of the player attacking
  public void attackAnimation() {
    //signals start of the attack and chooses ananimation
    if (PApplet.parseInt(attackFrame) == 0){
      soundSystem.attackSound();
      animationNumber = PApplet.parseInt(random(2));
    }
    //cycle through images
    attackFrame = (attackFrame + 0.35f) % 5;
    if (PApplet.parseInt(attackFrame) != 4) {
      //play first set of sprites
      if (animationNumber == 0) {
        //reposition player
        if (PApplet.parseInt(attackFrame) == 0)
          y -= 17;
        if (PApplet.parseInt(attackFrame) == 1) {
          x += 12;
          y -= 14;
        }
        if (PApplet.parseInt(attackFrame) == 2) {
          x -= 10;
          y -= 8;
        }
        if (PApplet.parseInt(attackFrame) == 3) {
          x -= 9;
          y -= 8;
        }
        image(attackAnimation1[PApplet.parseInt(attackFrame)], x, y);
        y = 300;
        x = 100;
      } 
      //play second set of sprites
      else {
        //reposition player
        if (PApplet.parseInt(attackFrame) == 0) {
          y -= 25;
        }
        if (PApplet.parseInt(attackFrame) == 1) {
          y -= 45;
        }
        if (PApplet.parseInt(attackFrame) == 2) {
          y -= 11;
        }
        if (PApplet.parseInt(attackFrame) == 3) {
          y += 12;
        }
        image(attackAnimation2[PApplet.parseInt(attackFrame)], x, y);
        y = 300;
        x = 100;
      }
    } 
    //signal the end of the attack so animation stops
    else {
      keys.attackFinished = true;
      keys.attacking = false;
      attackFrame = 0;
    }
  }
  //cycle through images of the player blocking
  public void blockAnimation() {
    if (PApplet.parseInt(blockFrame) == 4) {
      //cycle through images
      blockFrame2 = (blockFrame2 + 0.5f) % 6;
      //reposition player
      if (blockFrame == 0)
        x = 98;
      else if (blockFrame == 1)
        x = 101;
      else if (blockFrame == 2)
        x = 109;
      else if (blockFrame == 3)
        x = 102;
      else if (blockFrame == 4)
        x = 96;
      else if (blockFrame == 5)
        x = 102;
      image(blockAnimation2[PApplet.parseInt(blockFrame2)], x, y);
    }
    //prepare to block (to delay the blocking animation
    else {
      //cycle through images
      blockFrame = (blockFrame + 0.3f) % 5;
      if (PApplet.parseInt(blockFrame) == 0 || PApplet.parseInt(blockFrame)== 1)
        y -=35;
      if(PApplet.parseInt(blockFrame) != 4)
        image(blockAnimation1[PApplet.parseInt(blockFrame)], x, y);
      y = 300;
    }
    //draw sparks
    if(PApplet.parseInt(blockFrame) == 0){
      soundSystem.blockSound();
      image(blockLightning[0], x + 125, 290);
    }
    if(PApplet.parseInt(blockFrame) == 1)
      image(blockLightning[1], x + 125, 290);
    if(PApplet.parseInt(blockFrame) == 3)
      image(blockLightning[2], x + 120, 300);
    // draw the lightning that blocks fireballs
    if(PApplet.parseInt(blockFrame) == 4){
      image(blockLightning[PApplet.parseInt(random(2, 6))], x + 120, 300);
      image(blockLightning[PApplet.parseInt(random(6, 8))], x + 100, 345);
    }
  }
  //cycle through images for the special move animation
  //void specialMoveAnimation(){
  //  //get player on the ground
  //  y = yMin;
  //  specialMoveFinished = false;
  //  //freeze the background
  //  bg.bgDelta = 0;
  //  //get player into the position for special move
  //  if(specialMoveStartPosition == false)
  //    specialMoveStartPosition();
  //  //charge the special move
  //  else if(specialMoveCharged == false)
  //    specialMoveCharging();
  //  //perform the special move
  //  else if(specialMoveFinished == false)
  //    specialMoveActivate();
  //}
  
  //void specialMoveStartPosition() {
  //  //cycle through images
  //  specialMoveFrame = (specialMoveFrame*10 - 2)/10;
  //  image(specialMoveAnimation[int(specialMoveFrame)], x, y);
  //  image(lightning[int(random(0,4))], 20, 200);
  //  //indicates the stage is over
  //  if (specialMoveFrame == 0){
  //    specialMoveStartPosition = true;
  //    timer = millis();
  //  }
  //}
  
  //void specialMoveCharging(){
  //  //cycle through images
  //  specialMoveFrame = (specialMoveFrame + 0.2) % 2;
  //  image(specialMoveAnimation[int(specialMoveFrame)], x, y);
  //  //draw random electric image
  //  if(int(specialMoveFrame) < 2)
  //    image(specialMoveAnimation[int(random(7, 10))], x - 10, y + 20);
  //  //indicates the stage is over
  //  if(millis() - timer >= 1000){
  //    specialMoveCharged = true;
  //    specialMoveFrame = 0;
  //    timer = millis();
  //  }
  //}
  
  //void specialMoveActivate(){
  //  //move background again bu at a faster rate
  //  bg.bgDelta = 5;
  //  //draw player and lightning alternating between the two randomly
  //  image(specialMoveAnimation[int(random(5,7))], x, y);
  //  image(specialMoveAnimation[int(random(10,14))], x + 70, y);
  //  //indicates stage is over after ten seconds
  //  if(millis() - timer >= 10000){
  //    specialMoveFinished = true;
  //    bg.bgDelta = 0.5;
  //    energy = 0;
  //  }
  //}
}
class RangedVillan extends Enemy{
  //constructor
  RangedVillan(){
    type = 2;
    x = 1540;
    y = 280;
    spriteSetup();
  }
  //methods
  public void spriteSetup() {
    sprites = loadImage("rangedVillan.png");
    villan = new PImage[11];
    villan[0] = sprites.get(815, 0, 77, 106);
    villan[1] = sprites.get(740, 0, 75, 106);
    villan[2] = sprites.get(645, 0, 95, 106);
    villan[3] = sprites.get(540, 0, 105, 106);
    villan[4] = sprites.get(450, 0, 90, 106);
    villan[5] = sprites.get(365, 0, 85, 106);
    villan[6] = sprites.get(280, 0, 85, 106);
    villan[7] = sprites.get(175, 0, 105, 106);
    villan[8] = sprites.get(70, 0, 107, 106);
    villan[9] = sprites.get(0, 0, 70, 106); 
    villan[10] = loadImage("marioStance.png");
  }
}
class Sound {
  //members
  SoundFile backgroundSong;
  SoundFile tutorialSong;
  SoundFile jump;
  SoundFile attack;
  SoundFile block;
  SoundFile damageTaken;
  SoundFile begin;
  SoundFile end[];
  SoundFile quotes [];
  SoundFile killAnnounce[];
  Boolean endPlayed;
  //constructor
  Sound(){
    menu.characterPath = join(loadStrings("character.txt"), "");
    endPlayed = false;
    backgroundSong = new SoundFile(Bloodlust.this, menu.characterPath + "backgroundSong.wav");
    tutorialSong = new SoundFile(Bloodlust.this, "tutorialSong.wav");
    jump = new SoundFile(Bloodlust.this, menu.characterPath + "jump.wav");
    attack = new SoundFile(Bloodlust.this, menu.characterPath + "attack.wav");
    block = new SoundFile(Bloodlust.this, menu.characterPath + "block.wav");
    damageTaken = new SoundFile(Bloodlust.this, menu.characterPath + "damageTaken.wav");
    begin = new SoundFile(Bloodlust.this, "begin.wav");
    //cycle through soundfiles and place them in the correct arrays
    end = new SoundFile[4];
    for(int i = 0; i < 4; i++)
      end[i] = new SoundFile(Bloodlust.this, "end" + (i+1) + ".wav");
    quotes = new SoundFile[4];
    for(int i = 0; i < 4; i++)
      quotes[i] = new SoundFile(Bloodlust.this, menu.characterPath + "quote" + (i+1) + ".wav");
    killAnnounce = new SoundFile[12];
    for(int i = 0; i < 12; i++)
      killAnnounce[i] = new SoundFile(Bloodlust.this, "killAnnounce" + (i+1) + ".wav");
  }
  //methods
  //play the audio files
  public void backgroundSong(){
    if(menu.music)
      backgroundSong.loop();
  }
  public void tutorialSong(){
    if(menu.music)
    tutorialSong.loop();
  }
  public void jumpSound(){
    if(menu.gameplay)
      jump.play();
  }
  public void attackSound(){
    if(menu.gameplay)
      attack.play();
  }
  public void blockSound(){
    if(menu.gameplay)
      block.play();
  }
  public void damageTakenSound(){
    if(menu.gameplay)
      damageTaken.play();
  }
  public void playQuote(){
    if(menu.gameplay)
      quotes[PApplet.parseInt(random(4))].play();
  }
  public void endSong(){
    if(menu.music)
      backgroundSong.stop();
  }
  public void endTutorial(){
    if(menu.music)
      tutorialSong.stop();
  }
  public void begin(){
    if(menu.gameplay)
      begin.play();
  }
  public void end(){
    //select andom audio file and play it only once
    if(menu.gameplay){
      if(endPlayed == false){
        end[PApplet.parseInt(random(4))].play();
        endPlayed = true;
      }
    }
  }
  public void announceFirstKill(){
    //play first audio file in the array
    if(menu.gameplay)
      killAnnounce[0].play();
  }
}
class Spawner {
  //members
  int timer;
  int spawnTime;
  int type;
  ArrayList<Enemy> enemies;
  //constructor
  Spawner(){
   spawnTime = PApplet.parseInt(random(800, 2000));
   enemies = new ArrayList<Enemy>();
  }
  //methods
  public void spawn() {
    //check it is time to spawn the enemy
    if(millis() - spawnTime >= timer){
      //decide which enemy to spawn
      type = PApplet.parseInt(random(3));
      //spawn a rock
      if(type == 0){
        enemies.add(new Obstacle());
        //begin coundown for next spawn
        spawnTime = PApplet.parseInt(random(800, 2000));
        timer = millis();
      }
      //spawn melee enemy
      else if(type == 1){
        enemies.add(new MeleeVillan());
        //begin coundown for next spawn
        spawnTime = PApplet.parseInt(random(800, 2000));
        timer = millis();
      }
      //spawn ranged enemy
      else{
        enemies.add(new RangedVillan());
        //begin coundown for next spawn
        spawnTime = PApplet.parseInt(random(800, 2000));
        timer = millis();
      }
    }
  }
  //spawn an obstacle
  public void spawnObstacles(){
     if(millis() - spawnTime >= timer){
        enemies.add(new Obstacle());
        //begin coundown for next spawn
        spawnTime = PApplet.parseInt(random(800, 2000));
        timer = millis();
     }
  }
  //spawn a melee enemy
    public void spawnMelee(){
     if(millis() - spawnTime >= timer){
        enemies.add(new MeleeVillan());
        //begin coundown for next spawn
        spawnTime = PApplet.parseInt(random(800, 2000));
        timer = millis();
     }
  }
  //spawn a ranged enemy
    public void spawnRanged(){
     if(millis() - spawnTime >= timer){
        enemies.add(new RangedVillan());
        //begin coundown for next spawn
        spawnTime = PApplet.parseInt(random(800, 2000));
        timer = millis();
     }
  }
  //draw enemies to the screen
  public void drawEnemies(){
    //cycle through the array list of enemies
    for(Enemy i : enemies){
      i.drawEnemy();
    }
  }
  //get rid of enemies
  public void despawn() {
    //cycle through the array list of enemies
    for(int j = 0; j < enemies.size(); j++){
      Enemy a = enemies.get(j);
      //collision detection for rocks
      if(a.type == 0){
        if(a.collided == false){
          if(a.x == -100)
            enemies.remove(j);
          if(a.villanFrame == 0){
            if(a.x < 100 & a.x > 21 & player.y > 280){
              a.collided = true;
              count.lives -= 1;
              soundSystem.damageTakenSound();
            }
          }
          else if(a.villanFrame == 1){
            if(a.x < 100 & a.x > 70 & player.y > 280){
              a.collided = true;
              count.lives -= 1;
              soundSystem.damageTakenSound();
            }
            else if(a.x < 70 & a.x > -38 & player.y > 255){
              a.collided = true;
              count.lives -= 1;
              soundSystem.damageTakenSound();
            }
          }
          else if(a.villanFrame == 2){
            if(a.x < 100 & a.x > 0 & player.y > 240){
            a.collided = true;
              count.lives -= 1;
            }
            else if(a.x < 0 & a.x > -35 & player.y > 260){
              a.collided = true;
              count.lives -= 1;
              soundSystem.damageTakenSound();
            }
          }
          else {
            if(a.x < 100 & a.x > 0 & player.y > 255){
              a.collided = true;
              count.lives -= 1;
              soundSystem.damageTakenSound();
            }
          }
        }
      }
      else{
        //collision detection for enemies
        if(a.x == 190 & keys.attacking == true){
          enemies.remove(j);
          count.killCount += 1;
          if(count.killCount == 1){
            soundSystem.announceFirstKill();
            kill.killed = true;
            kill.timer = millis();
          }
          else{
            kill.announcement = PApplet.parseInt(random(1,12));
            soundSystem.killAnnounce[kill.announcement].play();
            kill.killed = true;
            kill.timer = millis();
          }
        }
        //check melee enemy has attacked player
        else if(a.type == 1 & a.x == 120)
          count.lives -= 1;
        else if(a.x == 100){
          enemies.remove(j);
          count.lives -= 1;
          soundSystem.damageTakenSound();
        }
      }
    }
  }
}
class Tutorial{
  int stage;
  String instructions;
  String tips;
  String winCondition;
  PFont font;
  int previousLives;
  int previousKills;
  int timer;
    public void tutorialRestart(){
    //all initial values required to start the single player program
    soundSystem = new Sound();  
    soundSystem.tutorialSong();
    bg = new Background();
    count = new Count();
    keys = new KeyPresses();
    player = new Player();
    spawner = new Spawner();
    kill = new Kill();
    stage = 1;
    font = createFont("Informal Roman", 30);
    previousLives= count.lives;
    previousKills= count.killCount;
    timer = millis();
  }
  public void tutorial(){
    if(stage ==1){
      instructions = menu.text[22];
      tips = menu.text[23];
      winCondition = menu.text[24] + (10 - ((millis()-timer)/1000));
    }
    else if(stage ==2){
      instructions = menu.text[25];
      tips = menu.text[26];
      winCondition = menu.text[27] + count.killCount +"/3)";
    }
    else if(stage ==3){
      instructions = menu.text[28];
      tips = menu.text[29];
      winCondition = menu.text[30] + (count.killCount-previousKills) +"/3)";
    }
    else if(stage ==4){
      instructions = menu.text[31];
      tips = menu.text[32];
      winCondition = "";
    }
    textAlign(CENTER);
    PFont font = createFont("Informal Roman", 30);
    textFont(font);
    textSize(120);
    fill(0xffFFD700);
    bg.drawBackground();
    player.drawPlayer();
    if(stage == 1)
      spawner.spawnObstacles();
    else if(stage == 2)
      spawner.spawnMelee();
    else if(stage == 3)
      spawner.spawnRanged();
    else{
      spawner.spawn();
      if(keys.specialMoving){
        menu.startTutorial = false;
        soundSystem.endTutorial();
        menu.bgSong.loop();
      }
    }
    spawner.drawEnemies();
    keys.moves();
    spawner.despawn();
    incrementStage();
    fill(0);
    text(instructions, width/2 , 90);
    textSize(50);
    text(tips, width/2 , 140);
    fill(255,0,0);
    text(winCondition, width/2 , 180);
  }
  public void incrementStage(){
    if(stage == 1){
      if(millis() - timer >10000)
        stage +=1;
      else if(previousLives-count.lives != 0){
        previousLives = count.lives;
        timer = millis();
      }
    }
    else if(stage == 2){
      if(count.killCount == 3){
        stage +=1;
        previousLives = count.lives;
        previousKills = count.killCount;
      }
    }
    else if(stage == 3){
      if(count.killCount - previousKills == 3)
        stage +=1;
      else if(previousLives-count.lives != 0){
        previousKills = count.killCount;
        previousLives = count.lives;
      }
    }
  }
}
  public void settings() {  size(1530, 536); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Bloodlust" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
