# YoungsBook
자신이 읽었던 책들을 기록하는 앱

설치경로 -> https://play.google.com/store/apps/details?id=com.youngsbook

## 사용기술
MyBatis 3.5.7 , MySQL , Spring, AWS(EC2, RDS), Firebase
클라이언트 사용언어 : Koltin, 서버 사용언어 : Java

Let's Encrypt와 DuckDNS를 이용해 SSL 인증서를 받은뒤 적용하였습니다.

Firebase를 이용해 이용자들에게 푸시알림을 보낼수있습니다.

## 설명 

네트워크는 Retrofit 라이브러리를 이용해 연결하였습니다.

네트워크 연결시도시 연결이 되지 않는다면, 앱에 설치된 CRT의 유효기간이 만료된것일수 있습니다, 직접 PlayStore에 들어가서 어플리케이션을 업데이트 하는것을 추천합니다.

로그인시, 버전체크를해서 서버와 현재 설치된 앱의 버전이 다르면 업데이트를 유도합니다.

네트워크 연결시도중 ProgressBar를 띄워주며, ProgressBar가 나와있는동안 사용자는 클릭이 불가능합니다.

로그인후 메인화면 우측 하단의 (+) 버튼을 눌러서 자신이 읽었던 책을 추가할수있습니다.


