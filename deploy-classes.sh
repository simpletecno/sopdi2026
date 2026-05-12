#!/bin/bash
SERVER=jaguirre@192.169.177.27
REMOTE_DIR=/opt/tomcat/webapps/sopdi/WEB-INF/classes
REMOTE_DIRFX=/opt/tomcat/webapps/sopdi_fundamex/WEB-INF/classes
LOCAL_DIR=target/classes

echo "📤 Subiendo clases modificadas..."
rsync -avz --checksum $LOCAL_DIR/ $SERVER:/tmp/sopdi-classes/

echo "🚀 Copiando al directorio de Tomcat..."
ssh $SERVER "sudo cp -r /tmp/sopdi-classes/. $REMOTE_DIR/ && sudo chown -R tomcat:tomcat $REMOTE_DIR/"
ssh $SERVER "sudo cp -r /tmp/sopdi-classes/. $REMOTE_DIRFX/ && sudo chown -R tomcat:tomcat $REMOTE_DIR/"

echo "♻️  Recargando aplicación..."
ssh $SERVER "sudo touch /opt/tomcat/webapps/sopdi/WEB-INF/web.xml"
ssh $SERVER "sudo touch /opt/tomcat/webapps/sopdi_fundamex/WEB-INF/web.xml"

echo "✅ Clases actualizadas"
