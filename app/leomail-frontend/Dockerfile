FROM nginx:stable

COPY dist/web/browser/* /usr/share/nginx/html/
COPY src/configs/deployment/default.conf /etc/nginx/conf.d/default.conf
