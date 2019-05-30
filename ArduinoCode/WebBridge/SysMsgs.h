
#ifndef SYSMSGS_H
#define SYSMSGS_H


class SysMsgs {

   public:

      const static byte MAX_MSG_SIZE = 108;
      /* sends to web endpoint */
      const static byte TYPE_WEB = 1;
      /* sends to bluetooth dev */
      const static byte TYPE_BTD = 2;
      /* sends sms msg */
      const static byte TYPE_SMS = 3;
      
      /* c-tor */
      void SysMsgs();


   private:

      void SomeCall();

};
 
#endif
