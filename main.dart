import 'package:flutter/material.dart';
import 'package:audioplayers/audioplayers.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        body: LyricsPage(),
      ),
    );
  }
}

class LyricsPage extends StatefulWidget {
  @override
  _LyricsPageState createState() => _LyricsPageState();
}

class _LyricsPageState extends State<LyricsPage> {
  final List<String> lyrics = [
    '학교종이',
    '땡땡땡',
    '어서모이자',
    '선생님이',
    '우리를',
    '기다리신다'
  ];
  final List<String> sounds = [
    '솔솔라라',
    '솔솔미',
    '솔솔미미레',
    '솔솔라라',
    '솔솔미',
    '솔미레미도'
  ];
  int idx = 0;
  List<int> isClicked = [];
  AudioCache audioCache = AudioCache(prefix: '');

  // 각 음에 해당하는 파일 이름을 저장하는 맵
  final Map<String, String> soundMap = {
    '솔': 'FX_piano08.mp3',
    '라': 'FX_piano10.mp3',
    '미': 'FX_piano05.mp3',
    '레': 'FX_piano03.mp3',
    '도': 'FX_piano01.mp3',
  };


  final Map<String, String> lyricsToSoundMap = {
    '학': '솔',
    '교': '솔',
    '종': '라',
    '이': '라',
    '땡': '솔',
    '어': '솔',
    '서': '솔',
    '모': '미',
    '자': '레',
    '선': '솔',
    '생': '솔',
    '님': '라',
    '우': '솔',
    '리': '솔',
    '를': '미',
    '기': '미',
    '다': '레',

    '신': '도',
  };

  List<bool> isButtonEnabled = [];

  @override
  void initState() {
    super.initState();
    isClicked = List.filled(lyrics.join().length, 0);
    isButtonEnabled = List.filled(lyrics.join().length, true);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        mainAxisAlignment: MainAxisAlignment.start,
        children: [
          Center(
            child: Container(
              margin: const EdgeInsets.only(top: 60.0),
              padding: const EdgeInsets.symmetric(vertical: 8.0),
              child: const Text(
                '학교종이 땡땡땡',
                style: TextStyle(
                  fontSize: 40,
                  fontWeight: FontWeight.bold,
                  color: Colors.blueAccent,
                ),
              ),
            ),
          ),
          Expanded(
            child: ListView.builder(
              itemCount: lyrics.length,
              itemBuilder: (context, index) {
                return Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: lyrics[index]
                      .split('')
                      .asMap()
                      .entries
                      .map((entry) {
                    int charIndex = lyrics
                        .sublist(0, index)
                        .join()
                        .length + entry.key;

                    return GestureDetector(
                      onTap:isButtonEnabled[charIndex]
                        ? () {
                        setState(() {

                          // 해당 글자에 해당하는 음을 찾아서 소리 파일을 재생
                          String? sound = sounds[index][entry.key];
                          if (sound != null) {
                            String? soundFile = soundMap[sound];
                            if (soundFile != null) {
                              audioCache.play(soundFile);
                            }
                          }
                          if (isClicked[charIndex] != 1) {
                            if (charIndex == idx) {
                              isClicked[charIndex] = 1;
                              isButtonEnabled[charIndex] = false;
                              idx++;
                            } else {
                              isClicked[charIndex] = -1;
                            }
                          }
                        });
                      }
                      :null,

                      child: Padding(
                        padding: const EdgeInsets.symmetric(
                            horizontal: 4.0, vertical: 4.0),
                        child: Text(
                          entry.value,
                          style: TextStyle(
                            fontSize: 44,
                            letterSpacing: 24,
                            height: 2.2,
                            color: isClicked[charIndex] == 1
                                ? Colors.green
                                : isClicked[charIndex] == -1
                                ? Colors.red
                                : Colors.black,
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                );
              },
            ),
          ),
        ],
      ),
    );
  }
}
