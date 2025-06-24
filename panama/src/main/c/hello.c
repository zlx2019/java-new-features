#include "stdio.h"
#include "ctype.h"

void say_hello(char* str);
void say_hello(char* str){
    printf("Hello %s \n", str);
}

/// 将字符串转换为大写后返回
char* str_to_upper(char* str){
    char* orig = str;
    while(*str){
        *str = toupper((unsigned char) *str);
        str++;
    }
    return orig;
}


// 编译为动态链接库：
// clang -fcolor-diagnostics -fansi-escape-codes -g -shared -fPIC -O2 -o lib_c.dylib -v hello.c