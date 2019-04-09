#!/usr/bin/env bash
git pull origin master
mvn clean package
rm -rf /root/dc/data/pan
rm nohup*

nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=125 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup1.out 2>nohup1.err &
nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=126 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup2.out 2>nohup2.err &
nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=127 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup3.out 2>nohup3.err &
nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=128 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup4.out 2>nohup4.err &
nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=129 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup5.out 2>nohup5.err &
#nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=130 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup6.out 2>nohup6.err &
#nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=131 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup7.out 2>nohup7.err &
#nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=132 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup8.out 2>nohup8.err &
#nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=133 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup9.out 2>nohup9.err &
#nohup java -jar target/crawler-1.0-SNAPSHOT.jar --web-id=134 --jdbc-url="jdbc:mysql://10.24.11.134:3306/webcrawler?characterEncoding=UTF-8&useSSL=false&useAffectedRows=true&allowPublicKeyRetrieval=true" --username=root --password=123456 >nohup10.out 2>nohup10.err &