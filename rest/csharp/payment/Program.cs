using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using CamundaClient;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace FlowingRetailPayment
{
    public class Program
    {
        /**
         * Quite hacky to do that here and keep in a static. But an easy way to do for this simple demo. Probably prefer DI in a real application :-)
         **/
        public static CamundaEngineClient Camunda;

        public static void Main(string[] args)
        {
            Camunda = new CamundaEngineClient();
            Camunda.Startup();

            BuildWebHost(args).Run();
        }    

        public static IWebHost BuildWebHost(string[] args) =>
            WebHost.CreateDefaultBuilder(args)
                .UseStartup<Startup>()
                .Build();
    }
}
