use std::ffi::{CStr, CString};
use std::os::raw::c_char;

/// 一些简单的运算函数
#[unsafe(no_mangle)]
pub extern "C" fn add(x: i32, y: i32) -> i32 {
    x + y
}
#[unsafe(no_mangle)]
pub extern "C" fn mul(x: i32, y: i32) -> i32 {
    x * y
}

/// 字符串处理函数，传入一个C风格的字符串，并且返回C风格的字符串。
#[unsafe(no_mangle)]
pub extern "C" fn str_to_upper(input: *const c_char) -> *mut c_char {
    if input.is_null() {
        // 返回空指针
        return std::ptr::null_mut();
    }
    unsafe {
        // CString to &CStr
        let c_string = CStr::from_ptr(input);
        // 转换为 Rust 字符串
        // CStr to Rust &str
        let rs_string = match c_string.to_str() {
            Ok(v) => v,
            Err(_) => return std::ptr::null_mut()
        };
        println!("rust: input str: {}", rs_string);
        let text = format!("{}", rs_string.to_uppercase());
        match CString::new(text) {
            Ok(v) => v.into_raw(),
            Err(_) => std::ptr::null_mut()
        }
    }
}

/// 释放字符串内存
#[unsafe(no_mangle)]
pub extern "C" fn free_str(ptr: *mut c_char){
    if !ptr.is_null() {
        unsafe {
            let _ = CString::from_raw(ptr);
        }
    }
}

#[repr(C)]
#[derive(Debug)]
pub struct User {
    pub id: i64,
    pub age: i64
}

#[repr(C)]
#[derive(Debug)]
pub struct Grade {
    pub id: i64,
    pub class_ids: [i64; 3],
    pub user: User,
    pub class_ids_ptr: *mut i64,
    pub user_ptr: *mut User
}

#[unsafe(no_mangle)]
pub extern "C" fn grade_print(grade: Grade){
    println!("{:?}", grade);
}