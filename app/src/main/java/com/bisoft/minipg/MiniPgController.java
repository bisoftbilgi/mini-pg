package com.bisoft.minipg;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bisoft.minipg.dto.CheckPointDTO;
import com.bisoft.minipg.dto.PromoteDTO;
import com.bisoft.minipg.dto.ReBaseUpDTO;
import com.bisoft.minipg.dto.RewindDTO;
import com.bisoft.minipg.helper.CommandExecutor;
import com.bisoft.minipg.helper.LocalSqlExecutor;
import com.bisoft.minipg.helper.MiniPGHelper;
import com.bisoft.minipg.helper.MiniPGLocalSettings;
import com.bisoft.minipg.helper.SymmetricEncryptionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/minipg")
@RequiredArgsConstructor
@Slf4j
public class MiniPgController {

    private final MiniPGLocalSettings miniPGlocalSetings;
    private final MiniPGHelper miniPGHelper;
    private final SymmetricEncryptionUtil symmetricEncryptionUtil;

    @RequestMapping("/status")
    public @ResponseBody
    String status() {
        return "OK";
    }

    @RequestMapping(path = "/checkpoint", method = RequestMethod.GET)
    public @ResponseBody
    String checkpoint() {
        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
            miniPGlocalSetings.getPgCtlBinPath() + "psql", "-c", "CHECKPOINT;");
        return cellValues.toString();
    }

    @RequestMapping(path = "/promote", method = RequestMethod.POST)
    public @ResponseBody
    String promote(@RequestBody PromoteDTO promoteDTO) {
        PgVersion localVersion = PgVersion.valueOf(miniPGlocalSetings.getPgVersion());
        if (localVersion == PgVersion.V12X || localVersion == PgVersion.V13X || localVersion == PgVersion.V14X) {
            miniPGHelper.promoteV12(promoteDTO);
        } else {
            miniPGHelper.promoteV10();
        }
        return "OK";
    }

    @RequestMapping(path = "/removeSyncNode", method = RequestMethod.POST)
    public @ResponseBody
    String removeSyncNode(@RequestBody String slaveAppName) throws Exception {
        miniPGHelper.removeSyncNode(slaveAppName);
        return "OK";
    }

    @RequestMapping(path = "/rewind", method = RequestMethod.POST)
    public @ResponseBody
    String rewind(@RequestBody RewindDTO rewindDTO) {
        return miniPGHelper.doRewind(rewindDTO);
    }

    @RequestMapping(path = "/rebaseUp", method = RequestMethod.POST)
    public @ResponseBody
    String rebaseUp(@RequestBody ReBaseUpDTO rebaseUpDTO) {
        log.info("rebaseUp Called...");
        return miniPGHelper.doReBaseUp(rebaseUpDTO);
    }

    @RequestMapping(path = "/start", method = RequestMethod.GET)
    public @ResponseBody
    List<String> start() {
        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "start", "-w",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
        
        while (miniPGHelper.startContinues()){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }    
        }
        return cellValues;
    }

    @RequestMapping(path = "/stop", method = RequestMethod.GET)
    public @ResponseBody
    List<String> stop() {
        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "stop", "-w",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
        return cellValues;
    }

    @RequestMapping(path = "/pgstatus", method = RequestMethod.GET)
    public @ResponseBody
    List<String> pgstatus() {
        List<String> cellValues = (new CommandExecutor()).executeCommandSync(
                miniPGlocalSetings.getPgCtlBinPath() + "pg_ctl", "status",
                "-D" + miniPGlocalSetings.getPostgresDataPath());
        return cellValues;
    }

    @RequestMapping(path = "/encrypt-creditential", method = RequestMethod.POST)
    public @ResponseBody
    String encryptCreditential(@RequestBody String secret) {
        //log.info(symmetricEncryptionUtil.encrypt(secret));
        return symmetricEncryptionUtil.encrypt(secret);
    }

    @RequestMapping(path = "/execute-sql", method = RequestMethod.POST)
    public @ResponseBody
    String executeSQL(@RequestBody String sql) throws Exception {
        StringBuilder result = new StringBuilder();
        String[] cmd = {miniPGlocalSetings.getPgCtlBinPath()+"psql", "-c", "\"" + sql +"\""};
        for (String line : cmd) {
            result.append(line + "\n");
            log.info(line);
        }

        ArrayList<String> cellValues = new ArrayList<>();

        Process pb = Runtime.getRuntime().exec(cmd);
        int resultNum = pb.waitFor();

        String line;

        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(pb.getErrorStream()));


        while ((line = input.readLine()) != null) {
            cellValues.add(line);
        }
        input.close();

        while ((line = error.readLine()) != null) {
            cellValues.add(line);
        }
        error.close();

        for (String s : cellValues)
        {
            result.append(s);
            result.append("\n");
        }
        // Arrays.stream(IOUtils.toString(pb.getErrorStream()).split("\n")).forEach(errorLine -> log.error(errorLine));
        return result.toString();
    }

    @RequestMapping(path = "/vip-down", method = RequestMethod.GET)
    public @ResponseBody
    String vipDown() throws Exception {
        StringBuilder result = new StringBuilder();
        String[] cmd = {"/bin/bash", "-c", "sudo ip address del " + miniPGlocalSetings.getVipIp() + "/" + miniPGlocalSetings.getVipIpNetmask()
                        +" dev "+ miniPGlocalSetings.getVipInterface()};
        for (String line : cmd) {
            result.append(line + "\n");
            log.info(line);
        }

        ArrayList<String> cellValues = new ArrayList<>();

        Process pb = Runtime.getRuntime().exec(cmd);
        int resultNum = pb.waitFor();

        String line;

        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
        BufferedReader error = new BufferedReader(new InputStreamReader(pb.getErrorStream()));


        while ((line = input.readLine()) != null) {
            cellValues.add(line);
        }
        input.close();

        Arrays.stream(IOUtils.toString(pb.getErrorStream()).split("\n")).forEach(errorLine -> log.error(errorLine));

        return result.toString();
    }

    @RequestMapping(path = "/vip-up", method = RequestMethod.GET)
    public @ResponseBody
    String vipUp() throws Exception {
        String[] cmd = {"/bin/bash", "-c", "sudo ip address add "+ miniPGlocalSetings.getVipIp() + "/" + miniPGlocalSetings.getVipIpNetmask() 
                        +" dev "+ miniPGlocalSetings.getVipInterface()};
        StringBuilder result = new StringBuilder();
        for (String line : cmd) {
            result.append(line + "\n");
            log.info(line);
        }

        ArrayList<String> cellValues = new ArrayList<>();
        Process pb = Runtime.getRuntime().exec(cmd);
        int resultNum = pb.waitFor();

        String line;


        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));

        while ((line = input.readLine()) != null) {
            cellValues.add(line);
        }
        input.close();

        Arrays.stream(IOUtils.toString(pb.getErrorStream()).split("\n")).forEach(errorLine -> log.error(errorLine));

        String postVipUpResult = this.postVipUp();

        return result.toString()+"\n"+postVipUpResult;
    }

    private
    String postVipUp() throws Exception {
        String[] cmd = {"/bin/bash", "-c", "sudo " + miniPGlocalSetings.getPostVipUp()};
        StringBuilder result = new StringBuilder();
        for (String line : cmd) {
            result.append(line + "\n");
            log.info(line);
        }

        ArrayList<String> cellValues = new ArrayList<>();
        Process pb = Runtime.getRuntime().exec(cmd);
        int resultNum = pb.waitFor();

        String line;


        BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));

        while ((line = input.readLine()) != null) {
            cellValues.add(line);
        }
        input.close();

        Arrays.stream(IOUtils.toString(pb.getErrorStream()).split("\n")).forEach(errorLine -> log.error(errorLine));


        return result.toString();
    }

    @RequestMapping(path = "/pre-so", method = RequestMethod.GET)
    public @ResponseBody
    String preSwitchOver() {
        return this.miniPGHelper.prepareForSwitchOver();
    }    
    
    @RequestMapping(path = "/post-so", method = RequestMethod.POST)
    public @ResponseBody
    String postSwitchOver(@RequestBody PromoteDTO promoteDTO) throws Exception {
        return this.miniPGHelper.postSwitchOver(promoteDTO);
    }


}
