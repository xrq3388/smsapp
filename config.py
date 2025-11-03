"""
配置文件
"""

# 服务器配置
SERVER_HOST = '0.0.0.0'  # 监听所有网络接口
SERVER_PORT = 8888       # 服务器端口

# 验证码提取配置
CODE_PATTERNS = [
    r'验证码[是为：:]\s*(\d{4,8})',
    r'verification code[:\s]+(\d{4,8})',
    r'code[:\s]+(\d{4,8})',
    r'验证码\s*(\d{4,8})',
    r'(\d{6})',  # 6位纯数字
    r'(\d{4})',  # 4位纯数字
]

# 日志配置
LOG_FILE = 'verification_codes.txt'  # 验证码保存文件

