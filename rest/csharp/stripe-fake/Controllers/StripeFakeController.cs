using Microsoft.AspNetCore.Mvc;
using System;
using System.Threading;


namespace FlowingRetailPayment.Controllers
{
    public class StripeFakeController : ControllerBase
    {
        public static bool slow = false;
        public static Random random = new Random();

        public class CreateChargeRequest
        {
            public int amount;
        }

        public class CreateChargeResponse
        {
            public String transactionId;
            public String errorCode;
        }

        [HttpPost]
        [Route("/charge")]
        public IActionResult RetrievePayment([FromBody]CreateChargeRequest request)
        {
            CreateChargeResponse response = new CreateChargeResponse();

            int waitTimeMillis = 0;
            if (slow)
            {
                waitTimeMillis = random.Next(0, 60) * 1000; // up to 60 seconds
            }

            if (random.NextDouble() > 0.8d) // random error in 20% of the cases
            {
                response.errorCode = "credit card expired";
            }

            Console.WriteLine("Charge on credit card will take " + waitTimeMillis / 1000 + " seconds");
            Thread.Sleep(waitTimeMillis);

            response.transactionId = Guid.NewGuid().ToString();
            return Ok(response);
        }

    }    
}
