import threading, time 
 
class Foo: 
    def __init__(self): 
        self.allow_thread1=True 
        self.allow_thread2=True 
        self.important_task=False 
        threading.Thread(target=self.thread1).start() 
        threading.Thread(target=self.thread2).start() 
 
    def thread1(self): 
        loops=0 
        while self.allow_thread1:
            for i in range(10): 
                print (' thread1') 
                time.sleep(0.5) 
 
            self.important_task=True 
 
            for i in range(10): 
                print ('thread1 important task') 
                time.sleep(0.5) 
 
            self.important_task=False 
 
            loops+=1 
 
            if loops >= 2: 
                self.exit() 
 
            time.sleep(0.5) 
 
    def thread2(self): 
        while self.allow_thread2: 
            if not self.important_task: 
                print(' thread2') 
            time.sleep(0.5) 
 
    def exit(self): 
        self.allow_thread2=False 
        self.allow_thread1=False 
        print ('Bye bye') 
        exit() 
 
 
if __name__ == '__main__': 
    Foo() 
