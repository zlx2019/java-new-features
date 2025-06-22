#include "stdio.h"

// 输出内容
void say_hello(char* str);
void say_hello(char* str){
    printf("Hello %s \n", str);
}


// 编译为动态链接库：
// clang -fcolor-diagnostics -fansi-escape-codes -g -shared -fPIC -O2 -o hello_c.dylib -v hello.c