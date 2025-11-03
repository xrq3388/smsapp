"""
测试客户端 - 用于模拟手机发送短信到服务器
可以用来测试服务器是否正常工作
"""
import socket
import json
from datetime import datetime


def send_test_sms(server_ip, server_port, sender, content):
    """
    发送测试短信到服务器
    """
    try:
        # 创建socket连接
        client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        client_socket.connect((server_ip, server_port))
        
        print(f"已连接到服务器: {server_ip}:{server_port}")
        
        # 构造JSON数据
        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
        sms_data = {
            'sender': sender,
            'content': content,
            'timestamp': timestamp
        }
        
        # 发送数据
        message = json.dumps(sms_data, ensure_ascii=False)
        client_socket.send(message.encode('utf-8'))
        
        print(f"\n已发送测试短信:")
        print(f"  发件人: {sender}")
        print(f"  内容: {content}")
        print(f"  时间: {timestamp}")
        
        # 接收服务器响应
        response = client_socket.recv(1024).decode('utf-8')
        response_data = json.loads(response)
        
        print(f"\n服务器响应:")
        print(f"  状态: {response_data.get('status')}")
        print(f"  检测到验证码: {response_data.get('code_detected')}")
        if response_data.get('code'):
            print(f"  验证码: {response_data.get('code')}")
        
        client_socket.close()
        print("\n测试完成！")
        
    except Exception as e:
        print(f"错误: {str(e)}")


def main():
    """
    主函数
    """
    print("="*60)
    print("短信转发系统 - 测试客户端")
    print("="*60)
    
    # 服务器配置
    server_ip = input("\n请输入服务器IP地址 [默认: 127.0.0.1]: ").strip() or "127.0.0.1"
    server_port_str = input("请输入服务器端口 [默认: 8888]: ").strip() or "8888"
    
    try:
        server_port = int(server_port_str)
    except ValueError:
        print("错误：端口号必须是数字")
        return
    
    # 测试短信列表
    test_messages = [
        {
            'sender': '106900000000',
            'content': '【测试】您的验证码是123456，请在5分钟内使用。'
        },
        {
            'sender': '10690',
            'content': '您的验证码：654321，有效期10分钟。'
        },
        {
            'sender': '10086',
            'content': '【中国移动】verification code: 888888'
        },
        {
            'sender': '95555',
            'content': '尊敬的用户，您的动态验证码为9527，请勿泄露。'
        }
    ]
    
    print("\n" + "="*60)
    print("选择测试短信:")
    print("="*60)
    for i, msg in enumerate(test_messages, 1):
        print(f"{i}. {msg['sender']}: {msg['content']}")
    print(f"{len(test_messages)+1}. 自定义短信")
    print("0. 退出")
    
    choice = input("\n请选择 [1-{}]: ".format(len(test_messages)+1)).strip()
    
    if choice == '0':
        print("已退出")
        return
    
    try:
        choice_num = int(choice)
        
        if 1 <= choice_num <= len(test_messages):
            # 发送预设测试短信
            msg = test_messages[choice_num - 1]
            send_test_sms(server_ip, server_port, msg['sender'], msg['content'])
            
        elif choice_num == len(test_messages) + 1:
            # 自定义短信
            sender = input("\n请输入发件人号码: ").strip()
            content = input("请输入短信内容: ").strip()
            if sender and content:
                send_test_sms(server_ip, server_port, sender, content)
            else:
                print("发件人和内容不能为空")
        else:
            print("无效的选择")
            
    except ValueError:
        print("无效的输入")


if __name__ == '__main__':
    main()

