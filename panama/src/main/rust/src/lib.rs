use std::ffi::{CStr, CString};
use std::os::raw::c_char;

/// # 输出给定的字符串
/// c_char：表示C语言原始字符串类型，即指针类型。
/// 
/// CStr:  表示 c_char 字符串的引用。介于C语言字符串和Rust字符串之间的中间层。可以由 c_char 转换而来，并且可以转化为 CString。
/// 
/// CString: 表示一个拥有所有权、并且以 null 终止的 C 风格字符串。当需要从Rust返回一个C原始字符串类型时，则需要此类型来构建。
/// 
/// # Args
/// - `content`: 指针类型（C原始字符串类型）
#[unsafe(no_mangle)]
pub extern "C" fn say_hello(rwa_str: *const c_char) {
    if rwa_str.is_null() {
        // 空指针
        return;
    }
    unsafe {
        // 将C原始字符串，转换为中间层 &CStr
        let c_str = CStr::from_ptr(rwa_str);
        // 将 &CStr 转换为 Rust &str
        let rs_str = c_str.to_str().expect("rwa string to str fail");
        println!("hello {}", rs_str);
    }
}

/// # 返回一个字符串类型
#[unsafe(no_mangle)]
pub extern "C" fn ret_hello() -> *mut c_char {
    // 构建 CString类型，该类型会在堆上分配，并且添加 null 终止符.
    let raw_str = CString::new("Hello, Rust").unwrap();

    // println!("raw_str addr: {:?}", raw_str.as_ptr());

    // 转换为裸指针返回，这会转移出所有权，Rust不在管理这块内存
    // 需要调用者来负责释放此内存
    raw_str.into_raw()
}

/// # 将给定的字符串，转换大写，然后返回。
#[unsafe(no_mangle)]
pub extern "C" fn str_to_upper(input: *const c_char) -> *mut c_char {
    if input.is_null() {
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
        // println!("rust: input str: {}", rs_string);
        let text = format!("{}", rs_string.to_uppercase());
        match CString::new(text) {
            Ok(v) => v.into_raw(),
            Err(_) => std::ptr::null_mut()
        }
    }
}

/// # 释放内存
/// 由于调用方是Java，并没有提供类似于C free 的功能函数，所以需要提供内存释放的函数。
#[unsafe(no_mangle)]
pub extern "C" fn free_mem(ptr: *mut c_char){
    if !ptr.is_null() {
        unsafe {
            let _ = CString::from_raw(ptr);
        }
    }
}

/// # 加法运算
#[unsafe(no_mangle)]
pub extern "C" fn add(x: i32, y: i32) -> i32 {
    x + y
}
/// # 乘法运算
#[unsafe(no_mangle)]
pub extern "C" fn mul(x: i32, y: i32) -> i32 {
    x * y
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