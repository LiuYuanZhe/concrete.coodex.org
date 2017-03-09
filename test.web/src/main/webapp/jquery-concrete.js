'use strict';

(function (factory) {
    if (typeof exports === "object" && typeof module === "object") // CommonJS
        module.exports = factory(require('jquery'));
    else if (typeof define === "function" && define.amd) // AMD
        define(['jquery'], factory);
    else { // Plain browser env
        var self = this || window;
        self.concrete = factory(self.$, self);
    }
}(function ($, self) {

    /**
     * 根据方法参数数量重载方法
     * @param funcName
     * @param function_map
     * @returns {Function}
     */
    var overload = function (funcName, function_map) {

        return function () {
            var key = arguments.length.toString();
            var func = function_map[key];
            if (!func && typeof func !== "function") {
                throw new Error(
                    "not found function: " + funcName + " with " + key + " parameter(s).");
            }
            return func.apply(this, arguments);
        }
    };

    var default_configuration = {
        "root": "",
        "onError": function (code, msg) {
            alert("errorCode:" + code + "\nerrorMsg:" + msg);
        }
    };

    var configuration = default_configuration;

    var encode = function (any) {
        if (any === undefined || any === null)
            return "";
        else
            return encodeURIComponent(any.toString());
    }

    var invoke = function (executable) {
        if (executable) {
            var pathNodes = executable.path.split("/");
            var url = "";
            var param = $.extend({}, executable.param);
            for (var i = 0; i < pathNodes.length; i++) {
                var node = pathNodes[i];
                if (!node && node.trim() === "") continue;
                if (node.charAt(0) === "{") {
                    var key = node.substr(1, node.length - 2);
                    node = param[key];
                    delete param[key];
                }
                url += "/" + encode(node);
            }

            var data = {};
            if (Object.keys(param).length > 0) {
                var obj = param[Object.keys(param)[0]];
                data = {
                    data: typeof(obj) === 'string' ? obj : JSON.stringify(obj)
                }
            }

            return $.ajax($.extend({}, data, {
                url: configuration.root + url,
                type: executable.method,
                contentType: "application/json; charset=utf-8",
                dataType: "json",
                crossDomain: true,
                xhrFields: {
                    withCredentials: true
                }
            })).error(function (jx) {
                if (configuration.onError) {
                    var e = jx.responseJSON;
                    if(typeof e === "object"){
                        configuration.onError(e.code,e.msg);
                    } else {
                        configuration.onError(jx.status, jx.responseText);
                    }
                                        // configuration.onError(this, arguments);
                }
            });
        } else {
            throw new Error("executable object is null.");
        }
    };

    var modules = {};

    var clean = function (fullName) {
        var nodes = fullName.split(".");
        var result = undefined;
        for (var i = 0; i < nodes.length; i++) {
            if (nodes[i] && nodes[i].trim() !== "") {
                if (result) {
                    result = result + "." + nodes[i];
                } else {
                    result = nodes[i];
                }
            }
        }
        return result;
    };

    var parseModule = function (fullName) {
        var packageName = undefined;
        var nodes = clean(fullName).split(".");
        for (var i = 0; i < nodes.length - 1; i++) {
            if (packageName) {
                packageName += "." + nodes[i];
            } else {
                packageName = nodes[i];
            }
        }
        return {
            "package": packageName,
            "module": nodes[nodes.length - 1]
        }
    };

    var concrete = {
        "configure": function (config) {
            configuration = $.extend({}, default_configuration, config);
        },
        "module": function () {
            if (arguments.length == 0 || arguments.length > 2) {
                throw new Error("IllegalArgument, arguments must (moduleName) or (package.moduleName) or (moduleName, package).");
            }
            var fullName = arguments.length === 1 ? arguments[0] : (arguments[1] + "." + arguments[0]);
            var info = parseModule(fullName);
            var m = modules[info.module];
            var module = undefined;
            if (m) {
                if (info.package) {
                    module = m[info.package];
                } else {
                    if (Object.keys(m).length === 1) {
                        module = m[Object.keys(m)[0]];
                    }
                }
            }

            if (module)
                return module;

            throw new Error("No module found. " + fullName);
        }
    };


    /**
     * 注册一个模块，由代码生成器调用
     * @param moduleName 模块名，既Interface的ClassName
     * @param packageName 包名，既Interface的packageName
     * @param module 该模块的所有方法
     */
    var register = function (moduleName, packageName, module) {
        if (!modules[moduleName]) {
            modules[moduleName] = {};
        }
        modules[moduleName][packageName] = module;
    };

    register("Calc", "cc.coodex.practice.jaxrs.api", { "add": function (x, y) {return invoke({"path": "/Calc/add/{x}/{y}","param": {"x": x, "y": y},"method": "GET" });}});

    register("ServiceExample", "cc.coodex.practice.jaxrs.api", { "all": function () {return invoke({"path": "/A/ServiceB/Book/all","param": {},"method": "GET" });}, "bigStringTest": function (pathParam, toPost) {return invoke({"path": "/A/ServiceB/Book/bigStringTest/{pathParam}","param": {"pathParam": pathParam, "toPost": toPost},"method": "POST" });}, "tokenId": function () {return invoke({"path": "/A/ServiceB/Book/tokenId","param": {},"method": "GET" });}, "get": overload("get", {"2": function (author, price) {return invoke({"path": "/A/ServiceB/Book/{author}/{price}","param": {"author": author, "price": price},"method": "GET" });}, "1": function (bookId) {return invoke({"path": "/A/ServiceB/Book/{bookId}","param": {"bookId": bookId},"method": "GET" });}}), "update": function (bookId, book) {return invoke({"path": "/A/ServiceB/Book/{bookId}","param": {"bookId": bookId, "book": book},"method": "PUT" });}, "findByAuthorLike": function (author) {return invoke({"path": "/A/ServiceB/Book/authorLike/{author}","param": {"author": author},"method": "GET" });}, "delete": function (bookId) {return invoke({"path": "/A/ServiceB/Book/{bookId}","param": {"bookId": bookId},"method": "DELETE" });}, "checkRole": function () {return invoke({"path": "/A/ServiceB/Book/checkRole","param": {},"method": "GET" });}, "findByPriceLessThen": function (price) {return invoke({"path": "/A/ServiceB/Book/priceLessThen/{price}","param": {"price": price},"method": "GET" });}});

    if(self){
        self.concrete = concrete;
    }
    return concrete;

}));