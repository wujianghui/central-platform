FROM dev-docker-hub.tianhong.cn/library/alpine-oraclejdk8:slim-timezone8
VOLUME /tmp
ADD ./target/sc-gateway.jar app.jar
#RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Duser.timezone=Asia/Shanghai","-jar","/app.jar"]