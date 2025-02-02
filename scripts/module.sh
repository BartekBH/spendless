#!/bin/bash

set -e

if [ $# -ne 1 ]
  then
    echo "Wong number of parameters provided. Usage ./module.sh [NAME]";
    exit 1;
fi

NAME=$1
LOWER_NAME=`echo "$NAME" | tr '[:upper:]' '[:lower:]'`
UPPER_NAME=`echo ${LOWER_NAME^}`

cp -r src/main/scala/com/cleverhouse/spendless/budget ./src/main/scala/com/cleverhouse/spendless/$LOWER_NAME
cp -r src/test/scala/com/cleverhouse/spendless/budget ./src/test/scala/com/cleverhouse/spendless/$LOWER_NAME



find ./src/main/scala/com/cleverhouse/spendless/${LOWER_NAME} -type f -exec sed -i "s/budget/$LOWER_NAME/g" {} \;
find ./src/main/scala/com/cleverhouse/spendless/${LOWER_NAME} -type f -exec sed -i "s/Budget/$UPPER_NAME/g" {} \;
for NAME in `find ./src/main/scala/com/cleverhouse/spendless/${LOWER_NAME} -type f -name '*Budget*'`;
do
  mv $NAME ${NAME/Budget/$UPPER_NAME}
done;


find ./src/test/scala/com/cleverhouse/spendless/${LOWER_NAME} -type f -exec sed -i "s/budget/$LOWER_NAME/g" {} \;
find ./src/test/scala/com/cleverhouse/spendless/${LOWER_NAME} -type f -exec sed -i "s/Budget/$UPPER_NAME/g" {} \;
for NAME in `find ./src/test/scala/com/cleverhouse/spendless/${LOWER_NAME} -type f -name '*Budget*'`;
do
  mv $NAME ${NAME/Budget/$UPPER_NAME}
done;

echo "done"


