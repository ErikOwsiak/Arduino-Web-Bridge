
#ifndef SYSMSGS_H
#define SYSMSGS_H


class SysMsgs {

   public:

      const static byte MAX_MSG_SIZE = 128;
      const static byte TYPE_WEB = 1;
      const static byte TYPE_ADR = 2;
      
      /* c-tor */
      void SysMsgs();


   private:

      void SomeCall();

};
 
#endif
