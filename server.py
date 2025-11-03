"""
短信验证码接收服务端
监听TCP连接，接收来自手机客户端的验证码消息
"""
import socket
import json
import re
from datetime import datetime
from threading import Thread
import os


class SmsServer:
    def __init__(self, host='0.0.0.0', port=8888):
        """
        初始化服务器
        :param host: 监听地址，默认0.0.0.0（所有网络接口）
        :param port: 监听端口，默认8888
        """
        self.host = host
        self.port = port
        self.server_socket = None
        self.is_running = False
        self.verification_codes = []  # 存储验证码历史
        
    def extract_verification_code(self, message):
        """
        从短信内容中提取验证码
        支持多种常见格式：6位数字、4位数字等
        """
        # 常见验证码模式
        patterns = [
            r'验证码[是为：:]\s*(\d{4,8})',
            r'verification code[:\s]+(\d{4,8})',
            r'code[:\s]+(\d{4,8})',
            r'验证码\s*(\d{4,8})',
            r'(\d{6})',  # 6位纯数字
            r'(\d{4})',  # 4位纯数字
        ]
        
        for pattern in patterns:
            match = re.search(pattern, message, re.IGNORECASE)
            if match:
                return match.group(1)
        
        return None
    
    def handle_client(self, client_socket, client_address):
        """
        处理客户端连接
        """
        print(f"\n[连接] 客户端已连接: {client_address[0]}:{client_address[1]}")
        
        try:
            while self.is_running:
                # 接收数据
                data = client_socket.recv(4096)
                if not data:
                    break
                
                try:
                    # 解析JSON数据
                    message = data.decode('utf-8')
                    sms_data = json.loads(message)
                    
                    # 提取信息
                    sender = sms_data.get('sender', '未知')
                    content = sms_data.get('content', '')
                    timestamp = sms_data.get('timestamp', '')
                    
                    # 提取验证码
                    verification_code = self.extract_verification_code(content)
                    
                    # 显示信息
                    print("\n" + "="*60)
                    print(f"[新短信] 时间: {timestamp}")
                    print(f"[发件人] {sender}")
                    print(f"[内容] {content}")
                    
                    if verification_code:
                        print(f"\n>>> 验证码: {verification_code} <<<")
                        # 保存验证码
                        self.verification_codes.append({
                            'code': verification_code,
                            'sender': sender,
                            'content': content,
                            'time': datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                        })
                        # 保存到文件
                        self.save_code_to_file(verification_code, sender, content)
                    else:
                        print("\n[提示] 未检测到验证码")
                    
                    print("="*60)
                    
                    # 发送确认回复
                    response = json.dumps({
                        'status': 'success',
                        'code_detected': verification_code is not None,
                        'code': verification_code
                    })
                    client_socket.send(response.encode('utf-8'))
                    
                except json.JSONDecodeError:
                    print(f"[错误] 无法解析JSON数据: {message}")
                except Exception as e:
                    print(f"[错误] 处理消息时出错: {str(e)}")
        
        except ConnectionResetError:
            print(f"\n[断开] 客户端断开连接: {client_address[0]}:{client_address[1]}")
        except Exception as e:
            print(f"\n[错误] 处理客户端时出错: {str(e)}")
        finally:
            client_socket.close()
    
    def save_code_to_file(self, code, sender, content):
        """
        将验证码保存到文件
        """
        try:
            with open('verification_codes.txt', 'a', encoding='utf-8') as f:
                timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                f.write(f"{timestamp} | {sender} | {code} | {content}\n")
        except Exception as e:
            print(f"[错误] 保存验证码到文件失败: {str(e)}")
    
    def start(self):
        """
        启动服务器
        """
        try:
            # 创建socket
            self.server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            
            # 绑定地址和端口
            self.server_socket.bind((self.host, self.port))
            self.server_socket.listen(5)
            
            self.is_running = True
            
            # 获取本机IP地址
            hostname = socket.gethostname()
            local_ip = socket.gethostbyname(hostname)
            
            print("="*60)
            print("短信验证码接收服务器已启动")
            print("="*60)
            print(f"监听地址: {self.host}")
            print(f"监听端口: {self.port}")
            print(f"\n请在手机客户端中连接到:")
            print(f"  服务器地址: {local_ip}")
            print(f"  端口号: {self.port}")
            print("\n等待客户端连接...")
            print("="*60)
            
            # 接受客户端连接
            while self.is_running:
                try:
                    client_socket, client_address = self.server_socket.accept()
                    # 为每个客户端创建新线程
                    client_thread = Thread(
                        target=self.handle_client,
                        args=(client_socket, client_address)
                    )
                    client_thread.daemon = True
                    client_thread.start()
                except OSError:
                    break
                    
        except Exception as e:
            print(f"[错误] 服务器启动失败: {str(e)}")
        finally:
            self.stop()
    
    def stop(self):
        """
        停止服务器
        """
        self.is_running = False
        if self.server_socket:
            self.server_socket.close()
        print("\n[停止] 服务器已关闭")
    
    def show_history(self):
        """
        显示验证码历史
        """
        if not self.verification_codes:
            print("\n暂无验证码历史记录")
            return
        
        print("\n" + "="*60)
        print("验证码历史记录")
        print("="*60)
        for i, item in enumerate(self.verification_codes, 1):
            print(f"{i}. {item['time']} | {item['sender']} | 验证码: {item['code']}")
        print("="*60)


def main():
    """
    主函数
    """
    import sys
    
    # 默认端口
    port = 8888
    
    # 如果有命令行参数，使用指定端口
    if len(sys.argv) > 1:
        try:
            port = int(sys.argv[1])
        except ValueError:
            print("错误：端口号必须是数字")
            sys.exit(1)
    
    # 创建并启动服务器
    server = SmsServer(port=port)
    
    try:
        server.start()
    except KeyboardInterrupt:
        print("\n\n[提示] 收到终止信号，正在关闭服务器...")
        server.stop()


if __name__ == '__main__':
    main()

